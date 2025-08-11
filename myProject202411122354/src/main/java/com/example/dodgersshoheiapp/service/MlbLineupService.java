package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.dto.LineupResponse;
import com.example.dodgersshoheiapp.dto.PlayerEntry;
import com.example.dodgersshoheiapp.dto.Pitcher;
import com.example.dodgersshoheiapp.dto.TeamLineup;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MLB StatsAPI から先発投手とスタメンを取得してDTOに整形するService。
 * 参照:
 * - feed/live: https://statsapi.mlb.com/api/v1.1/game/{gamePk}/feed/live
 * - schedule:
 * https://statsapi.mlb.com/api/v1/schedule?sportId=1&teamId={teamId}&date={date}&hydrate=probablePitcher
 * - scheduleR:
 * https://statsapi.mlb.com/api/v1/schedule?sportId=1&teamId={teamId}&startDate={from}&endDate={to}&hydrate=probablePitcher
 */
@Service
public class MlbLineupService {

    private static final String FEED_URL = "https://statsapi.mlb.com/api/v1.1/game/{gamePk}/feed/live";

    private static final String SCHEDULE_URL = "https://statsapi.mlb.com/api/v1/schedule?sportId=1&teamId={teamId}&date={date}&hydrate=probablePitcher";

    private static final String SCHEDULE_RANGE_URL = "https://statsapi.mlb.com/api/v1/schedule?sportId=1&teamId={teamId}&startDate={from}&endDate={to}&hydrate=probablePitcher";

    // 強調表示したい日本人選手のpeopleId（必要に応じて増減）
    private static final Set<Long> JAPANESE_IDS = Set.of(
            660271L, // Shohei Ohtani
            808967L, // Yoshinobu Yamamoto
            684007L, // Shota Imanaga
            673548L, // Seiya Suzuki
            808963L // Roki Sasaki
    // 追加はここに
    );

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    /** gamePk 指定で feed/live を取得し、先発＆スタメンを整形 */
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

            TeamLineup home = new TeamLineup(homeName, homeProb, homeLineup);
            TeamLineup away = new TeamLineup(awayName, awayProb, awayLineup);
            return new LineupResponse(home, away);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse MLB live feed: " + e.getMessage(), e);
        }
    }

    /** 指定日の teamId の gamePk を schedule から取得（非Finalを優先。なければ先頭） */
    public OptionalLong findGamePkByDate(int teamId, java.time.LocalDate date) {
        String json = restTemplate.getForObject(
                SCHEDULE_URL,
                String.class,
                Map.of("teamId", teamId, "date", date.toString()));
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode dates = root.path("dates");
            if (!dates.isArray() || dates.size() == 0)
                return OptionalLong.empty();

            JsonNode games = dates.get(0).path("games");
            if (!games.isArray() || games.size() == 0)
                return OptionalLong.empty();

            // その日の非Finalを優先（ダブルヘッダー等に備え）
            Long nonFinal = null;
            for (var g : games) {
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

    /** from から lookaheadDays 先までの範囲で、最初の非Finalの gamePk を返す */
    public OptionalLong findNextGamePk(int teamId, java.time.LocalDate from, int lookaheadDays) {
        String json = restTemplate.getForObject(
                SCHEDULE_RANGE_URL,
                String.class,
                Map.of("teamId", teamId,
                       "from", from.toString(),
                       "to", from.plusDays(lookaheadDays).toString())
        );
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode dates = root.path("dates");
            if (!dates.isArray() || dates.size() == 0) return OptionalLong.empty();

            for (var d : dates) {
                JsonNode games = d.path("games");
                for (var g : games) {
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

    // ---------- 内部ユーティリティ ----------

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

        List<PlayerEntry> list = new ArrayList<>();
        Iterator<String> it = playersNode.fieldNames();
        while (it.hasNext()) {
            String key = it.next(); // "IDxxxxxx"
            JsonNode p = playersNode.get(key);

            String battingOrder = p.path("battingOrder").asText("");
            if (battingOrder.isEmpty())
                continue;

            int order = normalizeOrder(battingOrder); // "101" -> 1
            String pos = p.at("/position/abbreviation").asText("");

            long id = p.at("/person/id").asLong(0);
            String name = p.at("/person/fullName").asText("");
            boolean jp = JAPANESE_IDS.contains(id);

            list.add(new PlayerEntry(id, name, pos, order, jp));
        }

        return list.stream()
                .sorted(Comparator.comparingInt(PlayerEntry::order))
                .collect(Collectors.toList());
    }

    private int normalizeOrder(String raw) {
        try {
            int v = Integer.parseInt(raw); // 101, 102 ...
            return Math.max(1, v / 100);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }
}
