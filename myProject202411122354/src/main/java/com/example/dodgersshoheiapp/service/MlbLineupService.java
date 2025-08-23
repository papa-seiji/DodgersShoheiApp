package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.dto.GameInfo;
import com.example.dodgersshoheiapp.dto.LineupResponse;
import com.example.dodgersshoheiapp.dto.PlayerEntry;
import com.example.dodgersshoheiapp.dto.Pitcher;
import com.example.dodgersshoheiapp.dto.TeamLineup;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MlbLineupService {

    private static final String FEED_URL = "https://statsapi.mlb.com/api/v1.1/game/{gamePk}/feed/live";

    private static final String SCHEDULE_URL = "https://statsapi.mlb.com/api/v1/schedule?sportId=1&teamId={teamId}&date={date}&hydrate=probablePitcher";

    private static final String SCHEDULE_RANGE_URL = "https://statsapi.mlb.com/api/v1/schedule?sportId=1&teamId={teamId}&startDate={from}&endDate={to}&hydrate=probablePitcher";

    private static final ZoneId JST = ZoneId.of("Asia/Tokyo");

    private static final Set<Long> JAPANESE_IDS = Set.of(
            660271L, // 大谷 翔平
            808967L, // 山本 由伸
            684007L, // 今永 昇太
            673548L, // 鈴木 誠也
            808963L, // 佐々木 朗希
            673548L, // 吉田 正尚
            506433L, // ダルビッシュ 有
            4142421L, // 千賀 滉大
            4142423L, // 菅野 智之
            4872583L, // 青柳 晃洋
            5264585L, // 小笠原 慎之介
            829679L // 森井 翔太郎
    );

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    /** Build lineups and game info (JST time + official US date). */
    public LineupResponse fetchLineups(long gamePk) {
        String json = restTemplate.getForObject(FEED_URL, String.class, Map.of("gamePk", gamePk));
        try {
            JsonNode root = mapper.readTree(json);

            String homeName = root.at("/gameData/teams/home/name").asText("");
            String awayName = root.at("/gameData/teams/away/name").asText("");

            Pitcher homeProb = readProbable(root, "home");
            Pitcher awayProb = readProbable(root, "away");

            List<PlayerEntry> homeLineup = readLineup(root, "home");
            List<PlayerEntry> awayLineup = readLineup(root, "away");

            // venue
            String venue = root.at("/gameData/venue/name").asText("");
            if (venue.isEmpty()) {
                venue = root.at("/gameData/venue/venueName").asText("");
            }

            // start time (UTC -> JST)
            String dateTimeUtc = root.at("/gameData/datetime/dateTime").isMissingNode()
                    ? ""
                    : root.at("/gameData/datetime/dateTime").asText("");
            ZonedDateTime jstDateTime = null;
            if (!dateTimeUtc.isEmpty()) {
                jstDateTime = ZonedDateTime.parse(dateTimeUtc).withZoneSameInstant(JST);
            }

            // official US date
            String officialDateStr = root.at("/gameData/datetime/officialDate").asText("");
            LocalDate officialDate = null;
            if (!officialDateStr.isEmpty()) {
                officialDate = LocalDate.parse(officialDateStr);
            } else if (jstDateTime != null) {
                officialDate = jstDateTime.toLocalDate();
            }

            GameInfo gameInfo = new GameInfo(venue, jstDateTime, officialDate);

            TeamLineup home = new TeamLineup(homeName, homeProb, homeLineup);
            TeamLineup away = new TeamLineup(awayName, awayProb, awayLineup);
            return new LineupResponse(home, away, gameInfo);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse MLB live feed: " + e.getMessage(), e);
        }
    }

    /**
     * Find gamePk for a team on a specific date. Prefer non-Final when multiple (DH
     * etc.).
     */
    public OptionalLong findGamePkByDate(int teamId, LocalDate date) {
        String json = restTemplate.getForObject(
                SCHEDULE_URL, String.class, Map.of("teamId", teamId, "date", date.toString()));
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode dates = root.path("dates");
            if (!dates.isArray() || dates.size() == 0)
                return OptionalLong.empty();

            JsonNode games = dates.get(0).path("games");
            if (!games.isArray() || games.size() == 0)
                return OptionalLong.empty();

            Long nonFinal = null;
            for (JsonNode g : games) {
                String detailed = g.path("status").path("detailedState").asText("");
                long pk = g.path("gamePk").asLong(0);
                if (pk != 0 && !detailed.contains("Final")) {
                    nonFinal = pk;
                    break;
                }
            }
            if (nonFinal != null)
                return OptionalLong.of(nonFinal);

            long first = games.get(0).path("gamePk").asLong(0);
            return (first == 0) ? OptionalLong.empty() : OptionalLong.of(first);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse MLB schedule: " + e.getMessage(), e);
        }
    }

    /** Find first non-Final gamePk in [from, from+lookaheadDays]. */
    public OptionalLong findNextGamePk(int teamId, LocalDate from, int lookaheadDays) {
        String json = restTemplate.getForObject(
                SCHEDULE_RANGE_URL, String.class,
                Map.of("teamId", teamId, "from", from.toString(), "to", from.plusDays(lookaheadDays).toString()));
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode dates = root.path("dates");
            if (!dates.isArray() || dates.size() == 0)
                return OptionalLong.empty();

            for (JsonNode d : dates) {
                JsonNode games = d.path("games");
                for (JsonNode g : games) {
                    String detailed = g.path("status").path("detailedState").asText("");
                    long pk = g.path("gamePk").asLong(0);
                    if (pk != 0 && !detailed.contains("Final")) {
                        return OptionalLong.of(pk);
                    }
                }
            }
            return OptionalLong.empty();

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse MLB schedule range: " + e.getMessage(), e);
        }
    }

    /** Find last gamePk on or before 'toInclusive' (look back up to N days). */
    public OptionalLong findPrevGamePk(int teamId, LocalDate toInclusive, int lookbackDays) {
        String json = restTemplate.getForObject(
                SCHEDULE_RANGE_URL, String.class,
                Map.of("teamId", teamId,
                        "from", toInclusive.minusDays(lookbackDays).toString(),
                        "to", toInclusive.toString()));
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode dates = root.path("dates");
            if (!dates.isArray() || dates.size() == 0)
                return OptionalLong.empty();

            for (int i = dates.size() - 1; i >= 0; i--) {
                JsonNode games = dates.get(i).path("games");
                for (int j = games.size() - 1; j >= 0; j--) {
                    JsonNode g = games.get(j);
                    long pk = g.path("gamePk").asLong(0);
                    if (pk != 0) {
                        return OptionalLong.of(pk);
                    }
                }
            }
            return OptionalLong.empty();

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse MLB schedule range: " + e.getMessage(), e);
        }
    }

    // -------- helpers --------

    private Pitcher readProbable(JsonNode root, String side) {
        long id = root.at("/gameData/probablePitchers/" + side + "/id").asLong(0);
        String name = root.at("/gameData/probablePitchers/" + side + "/fullName").asText("");
        boolean jp = JAPANESE_IDS.contains(id);
        return new Pitcher(id, name, jp);
    }

    private List<PlayerEntry> readLineup(JsonNode root, String side) {
        JsonNode playersNode = root.at("/liveData/boxscore/teams/" + side + "/players");
        if (playersNode.isMissingNode() || !playersNode.isObject())
            return List.of();

        // collapse PH/PR and substitutions: keep one per batting slot (1..9)
        class Holder {
            PlayerEntry entry;
            int rawOrder;
            boolean isSub;
            String pos;

            Holder(PlayerEntry e, int raw, boolean sub, String pos) {
                this.entry = e;
                this.rawOrder = raw;
                this.isSub = sub;
                this.pos = pos;
            }
        }
        Map<Integer, Holder> best = new HashMap<>();

        Iterator<String> it = playersNode.fieldNames();
        while (it.hasNext()) {
            String key = it.next();
            JsonNode p = playersNode.get(key);

            String battingOrderStr = p.path("battingOrder").asText("");
            if (battingOrderStr.isEmpty())
                continue;

            int raw;
            try {
                raw = Integer.parseInt(battingOrderStr);
            } catch (NumberFormatException ex) {
                continue;
            }
            int slot = Math.max(1, raw / 100);
            if (slot > 9)
                slot = 9;

            String pos = p.at("/position/abbreviation").asText("");
            long id = p.at("/person/id").asLong(0);
            String name = p.at("/person/fullName").asText("");
            boolean jp = JAPANESE_IDS.contains(id);
            boolean isSub = p.at("/gameStatus/isSubstitute").asBoolean(false);

            PlayerEntry entry = new PlayerEntry(id, name, pos, slot, jp);

            Holder cur = best.get(slot);
            if (cur == null) {
                best.put(slot, new Holder(entry, raw, isSub, pos));
                continue;
            }
            boolean curBench = "PH".equals(cur.pos) || "PR".equals(cur.pos);
            boolean newBench = "PH".equals(pos) || "PR".equals(pos);

            boolean takeNew = false;
            if (cur.isSub && !isSub)
                takeNew = true;
            else if (curBench && !newBench)
                takeNew = true;
            else if (raw < cur.rawOrder)
                takeNew = true;

            if (takeNew)
                best.put(slot, new Holder(entry, raw, isSub, pos));
        }

        return best.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getValue().entry)
                .collect(Collectors.toList());
    }
}
