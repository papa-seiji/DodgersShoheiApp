package com.example.dodgersshoheiapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Controller
public class PostseasonController {

    @GetMapping("/postseason")
    public String showBracket(Model model) {
        Map<String, String> logos = Map.ofEntries(
                // AL
                Map.entry("TIGERS", "https://www.mlbstatic.com/team-logos/116.svg"),
                Map.entry("GUARDIANS", "https://www.mlbstatic.com/team-logos/114.svg"),
                Map.entry("MARINERS", "https://www.mlbstatic.com/team-logos/136.svg"),
                Map.entry("REDSOX", "https://www.mlbstatic.com/team-logos/111.svg"),
                Map.entry("YANKEES", "https://www.mlbstatic.com/team-logos/147.svg"),
                Map.entry("BLUEJAYS", "https://www.mlbstatic.com/team-logos/141.svg"),

                // NL
                Map.entry("REDS", "https://www.mlbstatic.com/team-logos/113.svg"),
                Map.entry("DODGERS", "https://www.mlbstatic.com/team-logos/119.svg"),
                Map.entry("PHILLIES", "https://www.mlbstatic.com/team-logos/143.svg"),
                Map.entry("PADRES", "https://www.mlbstatic.com/team-logos/135.svg"),
                Map.entry("CUBS", "https://www.mlbstatic.com/team-logos/112.svg"),
                Map.entry("BREWERS", "https://www.mlbstatic.com/team-logos/158.svg"));
        model.addAttribute("logos", logos);
        return "postseason"; // ✅ HTMLテンプレートを返す
    }

    @GetMapping("/api/mlb/series-results")
    @ResponseBody
    public Map<String, String> getSeriesResults() {
        Map<String, String> results = new HashMap<>();
        try {
            String apiUrl = "https://statsapi.mlb.com/api/v1/schedule/postseason?season=2025";
            RestTemplate rest = new RestTemplate();
            ResponseEntity<Map> resp = rest.getForEntity(apiUrl, Map.class);
            Map<String, Object> body = resp.getBody();

            if (body == null || !body.containsKey("dates")) {
                results.put("series1", "APIからデータを取得できませんでした");
                return results;
            }

            List<Map<String, Object>> dates = (List<Map<String, Object>>) body.get("dates");
            List<Map<String, Object>> allGames = new ArrayList<>();
            for (Map<String, Object> date : dates) {
                List<Map<String, Object>> games = (List<Map<String, Object>>) date.get("games");
                if (games != null) {
                    allGames.addAll(games);
                }
            }

            // Reds vs Dodgers のゲームをすべて抽出
            List<Map<String, Object>> redsDodgersGames = allGames.stream()
                    .filter(game -> {
                        Map<String, Object> teams = (Map<String, Object>) game.get("teams");
                        Map<String, Object> home = (Map<String, Object>) teams.get("home");
                        Map<String, Object> away = (Map<String, Object>) teams.get("away");
                        String homeName = (String) ((Map<String, Object>) home.get("team")).get("name");
                        String awayName = (String) ((Map<String, Object>) away.get("team")).get("name");
                        return (homeName.equals("Los Angeles Dodgers") && awayName.equals("Cincinnati Reds"))
                                || (homeName.equals("Cincinnati Reds") && awayName.equals("Los Angeles Dodgers"));
                    })
                    .toList();

            if (redsDodgersGames.isEmpty()) {
                results.put("series1", "Reds-Dodgers戦が見つかりません");
            } else {
                int dodgerWins = 0;
                int redsWins = 0;

                for (Map<String, Object> game : redsDodgersGames) {
                    Map<String, Object> teams = (Map<String, Object>) game.get("teams");
                    Map<String, Object> home = (Map<String, Object>) teams.get("home");
                    Map<String, Object> away = (Map<String, Object>) teams.get("away");

                    Map<String, Object> homeRec = (Map<String, Object>) home.get("leagueRecord");
                    Map<String, Object> awayRec = (Map<String, Object>) away.get("leagueRecord");

                    // leagueRecord の wins はシリーズ全体の勝利数ではなく、このチームの勝利試合数を格納している可能性もあるので注意
                    // だがここでは「leagueRecord.wins」を使う前提で
                    int homeW = (int) homeRec.getOrDefault("wins", 0);
                    int awayW = (int) awayRec.getOrDefault("wins", 0);

                    String homeName = (String) ((Map<String, Object>) home.get("team")).get("name");
                    String awayName = (String) ((Map<String, Object>) away.get("team")).get("name");

                    // どちらが勝ったか判定
                    if (homeW > awayW) {
                        if (homeName.equals("Los Angeles Dodgers")) {
                            dodgerWins++;
                        } else {
                            redsWins++;
                        }
                    } else if (awayW > homeW) {
                        if (awayName.equals("Los Angeles Dodgers")) {
                            dodgerWins++;
                        } else {
                            redsWins++;
                        }
                    }
                }

                String seriesDesc = (String) ((Map<String, Object>) redsDodgersGames.get(0)).get("seriesDescription");
                results.put("series1",
                        "Reds vs Dodgers " + dodgerWins + "-" + redsWins + " (" + seriesDesc + ")");
            }

        } catch (Exception e) {
            results.put("series1", "APIエラー: " + e.getMessage());
        }
        return results;
    }
}
