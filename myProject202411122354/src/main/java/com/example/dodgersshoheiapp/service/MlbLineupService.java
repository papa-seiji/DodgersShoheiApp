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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MlbLineupService {

    private static final String FEED_URL = "https://statsapi.mlb.com/api/v1.1/game/{gamePk}/feed/live";
    private static final String SCHEDULE_URL = "https://statsapi.mlb.com/api/v1/schedule?sportId=1&teamId={teamId}&date={date}&hydrate=probablePitcher";
    private static final String SCHEDULE_RANGE_URL = "https://statsapi.mlb.com/api/v1/schedule?sportId=1&teamId={teamId}&startDate={from}&endDate={to}&hydrate=probablePitcher";
    private static final ZoneId JST = ZoneId.of("Asia/Tokyo");

    // Highlighted Japanese players (peopleId)
    private static final Set<Long> JAPANESE_IDS = Set.of(
            660271L, // Shohei Ohtani
            808967L, // Yoshinobu Yamamoto
            684007L, // Shota Imanaga
            673548L, // Seiya Suzuki
            808963L // Roki Sasaki
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

            // Venue
            String venue = root.at("/gameData/venue/name").asText("");
            if (venue.isEmpty()) {
                venue = root.at("/gameData/venue/venueName").asText("");
            }

            // Start time (UTC -> JST)
            String dateTimeUtc = root.at("/gameData/datetime/dateTime").asText("");
            ZonedDateTime jstDateTime = null;
            if (!dateTimeUtc.isEmpty()) {
                jstDateTime = ZonedDateTime.parse(dateTimeUtc).withZoneSameInstant(JST);
            }

            // Official US date (used for display date)
            String officialDateStr = root.at("/gameData/datetime/officialDate").asText("");
            LocalDate officialDate = null;
            if (!officialDateStr.isEmpty()) {
                officialDate = LocalDate.parse(officialDateStr);
            } else if (jstDateTime != null) {
                // Fallback if officialDate missing
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

    /** Find gamePk for a team on a specific date. Prefer non-Final. */
    public OptionalLong findGamePkByDate(int teamId, LocalDate date) {
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

    /** Find the first non-Final gamePk in [from, from+lookaheadDays]. */
    public OptionalLong findNextGamePk(int teamId, LocalDate from, int lookaheadDays) {
        String json = restTemplate.getForObject(
                SCHEDULE_RANGE_URL,
                String.class,
                Map.of(
                        "teamId", teamId,
                        "from", from.toString(),
                        "to", from.plusDays(lookaheadDays).toString()));
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

        List<PlayerEntry> list = new ArrayList<>();
        Iterator<String> it = playersNode.fieldNames();
        while (it.hasNext()) {
            String key = it.next();
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
            int v = Integer.parseInt(raw); // 101, 102, ...
            return Math.max(1, v / 100);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

}
