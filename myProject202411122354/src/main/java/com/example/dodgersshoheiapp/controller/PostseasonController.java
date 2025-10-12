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
                results.put("error", "APIからデータを取得できませんでした");
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

            // ✅ ① Reds vs Dodgers（Wild Card）
            String redsDodgers = summarizeSeries(allGames, "Cincinnati Reds", "Los Angeles Dodgers");
            results.put("series1", redsDodgers);

            // ✅ ② Dodgers vs Phillies（NLDS）
            String dodgersPhillies = summarizeSeries(allGames, "Los Angeles Dodgers", "Philadelphia Phillies");
            results.put("series2", dodgersPhillies);

            // ✅ ③ Cubs vs Padres（Wild Card）
            String cubsPadres = summarizeSeries(allGames, "Chicago Cubs", "San Diego Padres");
            results.put("series3", cubsPadres);

            // ✅ ④ Brewers vs Cubs（NLDS）
            String brewersCubs = summarizeSeries(allGames, "Milwaukee Brewers", "Chicago Cubs");
            results.put("series4", brewersCubs);

        } catch (Exception e) {
            results.put("error", "APIエラー: " + e.getMessage());
        }
        return results;
    }

    /**
     * ✅ 指定2チームのシリーズ結果を集計
     */
    private String summarizeSeries(List<Map<String, Object>> allGames, String teamA, String teamB) {
        List<Map<String, Object>> targetGames = allGames.stream()
                .filter(game -> {
                    Map<String, Object> teams = (Map<String, Object>) game.get("teams");
                    Map<String, Object> home = (Map<String, Object>) teams.get("home");
                    Map<String, Object> away = (Map<String, Object>) teams.get("away");
                    String homeName = (String) ((Map<String, Object>) home.get("team")).get("name");
                    String awayName = (String) ((Map<String, Object>) away.get("team")).get("name");
                    return (homeName.equals(teamA) && awayName.equals(teamB))
                            || (homeName.equals(teamB) && awayName.equals(teamA));
                })
                .toList();

        if (targetGames.isEmpty()) {
            return teamA + " vs " + teamB + " 試合なし";
        }

        int winsA = 0;
        int winsB = 0;
        String seriesDesc = (String) targetGames.get(0).get("seriesDescription");

        for (Map<String, Object> game : targetGames) {
            Map<String, Object> teams = (Map<String, Object>) game.get("teams");
            Map<String, Object> home = (Map<String, Object>) teams.get("home");
            Map<String, Object> away = (Map<String, Object>) teams.get("away");

            String homeName = (String) ((Map<String, Object>) home.get("team")).get("name");
            String awayName = (String) ((Map<String, Object>) away.get("team")).get("name");

            // ✅ まず teams.home.score / teams.away.score を確認
            Integer homeRuns = (Integer) home.get("score");
            Integer awayRuns = (Integer) away.get("score");

            // ✅ null の場合は linescore から取得（後方互換）
            if (homeRuns == null || awayRuns == null) {
                Map<String, Object> linescore = (Map<String, Object>) game.get("linescore");
                if (linescore != null && linescore.containsKey("teams")) {
                    Map<String, Object> scoreTeams = (Map<String, Object>) linescore.get("teams");
                    Map<String, Object> homeScore = (Map<String, Object>) scoreTeams.get("home");
                    Map<String, Object> awayScore = (Map<String, Object>) scoreTeams.get("away");
                    homeRuns = (Integer) homeScore.getOrDefault("runs", 0);
                    awayRuns = (Integer) awayScore.getOrDefault("runs", 0);
                } else {
                    continue; // スコア情報がない試合はスキップ
                }
            }

            // ✅ 勝敗判定
            if (homeRuns > awayRuns) {
                if (homeName.equals(teamA))
                    winsA++;
                else
                    winsB++;
            } else if (awayRuns > homeRuns) {
                if (awayName.equals(teamA))
                    winsA++;
                else
                    winsB++;
            }
        }

        return teamA + " vs " + teamB + " " + winsA + "-" + winsB + " (" + seriesDesc + ")";
    }
}
