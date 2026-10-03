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

    /**
     * ✅ ポストシーズン画面（トーナメント表示）
     */
    @GetMapping("/postseason")
    public String showBracket(Model model) {
        Map<String, String> logos = Map.ofEntries(
                Map.entry("TIGERS", "https://www.mlbstatic.com/team-logos/116.svg"),
                Map.entry("GUARDIANS", "https://www.mlbstatic.com/team-logos/114.svg"),
                Map.entry("MARINERS", "https://www.mlbstatic.com/team-logos/136.svg"),
                Map.entry("REDSOX", "https://www.mlbstatic.com/team-logos/111.svg"),
                Map.entry("YANKEES", "https://www.mlbstatic.com/team-logos/147.svg"),
                Map.entry("BLUEJAYS", "https://www.mlbstatic.com/team-logos/141.svg"),
                Map.entry("REDS", "https://www.mlbstatic.com/team-logos/113.svg"),
                Map.entry("DODGERS", "https://www.mlbstatic.com/team-logos/119.svg"),
                Map.entry("PHILLIES", "https://www.mlbstatic.com/team-logos/143.svg"),
                Map.entry("PADRES", "https://www.mlbstatic.com/team-logos/135.svg"),
                Map.entry("CUBS", "https://www.mlbstatic.com/team-logos/112.svg"),
                Map.entry("BREWERS", "https://www.mlbstatic.com/team-logos/158.svg"));
        model.addAttribute("logos", logos);
        return "postseason";
    }

    /**
     * ✅ 2026 ポストシーズン画面
     */
    @GetMapping("/postseason_2026")
    public String showBracket2026(Model model) {

        Map<String, String> logos = Map.ofEntries(

                // =========================
                // American League
                // =========================
                Map.entry("RAYS",
                        "https://www.mlbstatic.com/team-logos/139.svg"),

                Map.entry("GUARDIANS",
                        "https://www.mlbstatic.com/team-logos/114.svg"),

                Map.entry("ASTROS",
                        "https://www.mlbstatic.com/team-logos/117.svg"),

                Map.entry("YANKEES",
                        "https://www.mlbstatic.com/team-logos/147.svg"),

                Map.entry("REDSOX",
                        "https://www.mlbstatic.com/team-logos/111.svg"),

                Map.entry("WHITESOX",
                        "https://www.mlbstatic.com/team-logos/145.svg"),

                // =========================
                // National League
                // =========================
                Map.entry("BREWERS",
                        "https://www.mlbstatic.com/team-logos/158.svg"),

                Map.entry("DODGERS",
                        "https://www.mlbstatic.com/team-logos/119.svg"),

                Map.entry("BRAVES",
                        "https://www.mlbstatic.com/team-logos/144.svg"),

                Map.entry("PADRES",
                        "https://www.mlbstatic.com/team-logos/135.svg"),

                Map.entry("CUBS",
                        "https://www.mlbstatic.com/team-logos/112.svg"),

                Map.entry("PHILLIES",
                        "https://www.mlbstatic.com/team-logos/143.svg"));

        model.addAttribute("logos", logos);

        return "postseason_2026";
    }

    /**
     * ✅ Postseason 成績取得（REST API版）
     * MLB statsapi から打撃・投球成績を直接取得
     */
    @GetMapping("/api/postseason/stats")
    @ResponseBody
    public Map<String, Object> getPostseasonStats() {
        Map<String, Object> results = new HashMap<>();
        RestTemplate rest = new RestTemplate();

        try {
            // ✅ 大谷翔平（打撃・投手）
            Object ohtaniHitting = rest.getForObject(
                    "https://statsapi.mlb.com/api/v1/people/660271/stats?stats=postseason&season=2025&group=hitting",
                    Object.class);
            Object ohtaniPitching = rest.getForObject(
                    "https://statsapi.mlb.com/api/v1/people/660271/stats?stats=postseason&season=2025&group=pitching",
                    Object.class);
            results.put("ohtaniHitting", ohtaniHitting);
            results.put("ohtaniPitching", ohtaniPitching);

            // ✅ 山本由伸（投手）
            Object yamamotoPitching = rest.getForObject(
                    "https://statsapi.mlb.com/api/v1/people/808967/stats?stats=postseason&season=2025&group=pitching",
                    Object.class);
            results.put("yamamotoPitching", yamamotoPitching);

            // ✅ 佐々木朗希（投手）
            Object sasakiPitching = rest.getForObject(
                    "https://statsapi.mlb.com/api/v1/people/808963/stats?stats=postseason&season=2025&group=pitching",
                    Object.class);
            results.put("sasakiPitching", sasakiPitching);

        } catch (Exception e) {
            results.put("error", "Postseason Stats API error: " + e.getMessage());
        }

        return results;
    }

    /**
     * ✅ シリーズ勝敗集計API
     */
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

            // ✅ 各シリーズ結果
            results.put("series1", summarizeSeries(allGames, "Cincinnati Reds", "Los Angeles Dodgers"));
            results.put("series2", summarizeSeries(allGames, "Los Angeles Dodgers", "Philadelphia Phillies"));
            results.put("series3", summarizeSeries(allGames, "Chicago Cubs", "San Diego Padres"));
            results.put("series4", summarizeSeries(allGames, "Milwaukee Brewers", "Chicago Cubs"));
            results.put("series5", summarizeSeries(allGames, "Los Angeles Dodgers", "Milwaukee Brewers"));
            results.put("series6", summarizeSeries(allGames, "Detroit Tigers", "Cleveland Guardians"));
            results.put("series7", summarizeSeries(allGames, "Boston Red Sox", "New York Yankees"));
            results.put("series8", summarizeSeries(allGames, "Seattle Mariners", "Detroit Tigers"));
            results.put("series9", summarizeSeries(allGames, "Toronto Blue Jays", "New York Yankees"));
            results.put("series10", summarizeSeries(allGames, "Seattle Mariners", "Toronto Blue Jays"));
            results.put("series11", summarizeSeries(allGames, "Los Angeles Dodgers", "Toronto Blue Jays"));

        } catch (Exception e) {
            results.put("error", "APIエラー: " + e.getMessage());
        }
        return results;
    }

    /**
     * ✅ 2026 シリーズ勝敗集計API
     */
    @GetMapping("/api/mlb/series-results-2026")
    @ResponseBody
    public Map<String, String> getSeriesResults2026() {

        Map<String, String> results = new HashMap<>();

        try {

            String apiUrl = "https://statsapi.mlb.com/api/v1/schedule/postseason?season=2026";

            RestTemplate rest = new RestTemplate();

            ResponseEntity<Map> resp = rest.getForEntity(apiUrl, Map.class);

            Map<String, Object> body = resp.getBody();

            if (body == null || !body.containsKey("dates")) {

                results.put(
                        "error",
                        "APIからデータを取得できませんでした");

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

            // ================================================
            // ✅ 2026 各シリーズ結果
            // ================================================

            // ---------- NL ----------

            // Wild Card
            results.put("series1", summarizeSeries(allGames, "Philadelphia Phillies", "Atlanta Braves"));

            // NLDS
            results.put("series2", summarizeSeries(allGames, "Atlanta Braves", "Los Angeles Dodgers"));

            // Wild Card
            results.put("series3", summarizeSeries(allGames, "San Diego Padres", "Chicago Cubs"));

            // NLDS
            results.put("series4", summarizeSeries(allGames, "San Diego Padres", "Milwaukee Brewers"));

            // ---------- AL ----------

            // Wild Card
            results.put("series6", summarizeSeries(allGames, "Chicago White Sox", "Houston Astros"));

            // ALDS
            results.put("series8", summarizeSeries(allGames, "Chicago White Sox", "Cleveland Guardians"));

            // Wild Card
            results.put("series7", summarizeSeries(allGames, "Boston Red Sox", "New York Yankees"));

            // ALDS
            results.put("series9", summarizeSeries(allGames, "New York Yankees", "Tampa Bay Rays"));

            // ---------- 未確定 ----------
            // series5 = NLCS
            // series10 = ALCS
            // series11 = World Series

            // ================================================
            // ⏳ 未確定
            // ================================================

            // series5 = NLCS
            // series10 = ALCS
            // series11 = World Series

            // 対戦カード確定後に追加する

        } catch (Exception e) {

            results.put(
                    "error",
                    "APIエラー: " + e.getMessage());
        }

        return results;
    }

    /**
     * ✅ チーム別シリーズ結果集計ロジック
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

        if (targetGames.isEmpty())
            return teamA + " vs " + teamB + "（試合未開始）";

        int winsA = 0;
        int winsB = 0;
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
            } else {
                if (awayName.equals(teamA))
                    winsA++;
                else
                    winsB++;
            }
        }

        String displayA = (winsA == 0) ? "0" : "🌟".repeat(winsA);
        String displayB = (winsB == 0) ? "0" : "🌟".repeat(winsB);
        return displayA + "-" + displayB;
    }
}
