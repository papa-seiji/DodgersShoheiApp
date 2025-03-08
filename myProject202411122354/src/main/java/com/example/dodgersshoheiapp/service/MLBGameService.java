package com.example.dodgersshoheiapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.*;

@Service
public class MLBGameService {
    private final String BASE_URL = "https://statsapi.mlb.com/api/v1/schedule/games/";

    public Map<String, Object> getFormattedGameInfo(String date) {
        RestTemplate restTemplate = new RestTemplate();
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("sportId", 1)
                .queryParam("teamId", 119)
                .queryParam("date", date)
                .toUriString();

        // JSONデータ取得
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || !response.containsKey("dates")) {
            return Collections.singletonMap("error", "試合情報が取得できませんでした");
        }

        // ゲームデータを取得
        List<Map<String, Object>> dates = (List<Map<String, Object>>) response.get("dates");
        if (dates.isEmpty()) {
            return Collections.singletonMap("error", "試合がありません");
        }

        List<Map<String, Object>> games = (List<Map<String, Object>>) dates.get(0).get("games");
        if (games.isEmpty()) {
            return Collections.singletonMap("error", "試合データなし");
        }

        Map<String, Object> game = games.get(0);
        Map<String, Object> awayTeam = (Map<String, Object>) ((Map<String, Object>) game.get("teams")).get("away");
        Map<String, Object> homeTeam = (Map<String, Object>) ((Map<String, Object>) game.get("teams")).get("home");

        // 必要なデータを整形
        Map<String, Object> formattedGame = new HashMap<>();
        formattedGame.put("date", game.get("gameDate"));
        formattedGame.put("status", game.get("status"));
        formattedGame.put("venue", ((Map<String, Object>) game.get("venue")).get("name"));
        formattedGame.put("away_team", ((Map<String, Object>) awayTeam.get("team")).get("name"));
        formattedGame.put("home_team", ((Map<String, Object>) homeTeam.get("team")).get("name"));
        formattedGame.put("away_score", awayTeam.getOrDefault("score", "N/A"));
        formattedGame.put("home_score", homeTeam.getOrDefault("score", "N/A"));

        return formattedGame;
    }
}
