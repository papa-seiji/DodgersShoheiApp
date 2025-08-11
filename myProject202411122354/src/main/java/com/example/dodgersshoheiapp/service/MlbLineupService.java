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
 * MLB StatsAPI (feed/live) から先発投手とスタメンを取得してDTOに整形するService。
 * 参照: https://statsapi.mlb.com/api/v1.1/game/{gamePk}/feed/live
 */
@Service
public class MlbLineupService {

    private static final String FEED_URL = "https://statsapi.mlb.com/api/v1.1/game/{gamePk}/feed/live";

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
