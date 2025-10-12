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
        return "postseason";
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

            // ✅ NL側
            results.put("series1", summarizeSeries(allGames, "Cincinnati Reds", "Los Angeles Dodgers")); // WC
            results.put("series2", summarizeSeries(allGames, "Los Angeles Dodgers", "Philadelphia Phillies")); // NLDS
            results.put("series3", summarizeSeries(allGames, "Chicago Cubs", "San Diego Padres")); // WC
            results.put("series4", summarizeSeries(allGames, "Milwaukee Brewers", "Chicago Cubs")); // NLDS
            results.put("series5", summarizeSeries(allGames, "Los Angeles Dodgers", "Milwaukee Brewers")); // NLCS

            // ✅ AL側
            results.put("series6", summarizeSeries(allGames, "Detroit Tigers", "Cleveland Guardians")); // AL WC
            results.put("series7", summarizeSeries(allGames, "Boston Red Sox", "New York Yankees")); // AL WC
            results.put("series8", summarizeSeries(allGames, "Seattle Mariners", "Detroit Tigers")); // ALDS ①
            results.put("series9", summarizeSeries(allGames, "Toronto Blue Jays", "New York Yankees")); // ALDS ②
            results.put("series10", summarizeSeries(allGames, "Seattle Mariners", "Toronto Blue Jays")); // ALCS

            // ✅ ⑪ World Series（NLCS勝者 vs ALCS勝者）
            results.put("series11", summarizeSeries(allGames, "Los Angeles Dodgers", "Seattle Mariners"));

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
            return teamA + " vs " + teamB + " （試合未開始）";
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

            Integer homeRuns = (Integer) home.get("score");
            Integer awayRuns = (Integer) away.get("score");

            if (homeRuns == null || awayRuns == null)
                continue;

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
