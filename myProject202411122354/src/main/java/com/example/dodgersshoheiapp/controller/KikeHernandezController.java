package com.example.dodgersshoheiapp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Controller
public class KikeHernandezController {

    @GetMapping("/kike")
    public String showKikeHernandezPage(Model model) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        // 🎯 2025年成績を取得
        try {
            String url = "https://statsapi.mlb.com/api/v1/people/571771/stats?stats=season&season=2025";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = mapper.readTree(response);
            JsonNode stats = root.at("/stats/0/splits/0/stat");

            model.addAttribute("stats", new PlayerStats(
                    stats.path("gamesPlayed").asInt(),
                    stats.path("avg").asText(),
                    stats.path("homeRuns").asInt(),
                    stats.path("rbi").asInt(),
                    stats.path("ops").asText()));
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("stats", new PlayerStats(0, "-", 0, 0, "-"));
        }

        // 🎯 2014〜2025 Career Stats を取得
        List<CareerStatRow> careerStats = new ArrayList<>();
        for (int year = 2014; year <= 2025; year++) {
            try {
                String url = "https://statsapi.mlb.com/api/v1/people/571771/stats?stats=season&season=" + year
                        + "&group=hitting";
                String response = restTemplate.getForObject(url, String.class);
                JsonNode root = mapper.readTree(response);
                JsonNode split = root.at("/stats/0/splits/0");

                if (!split.isMissingNode()) {
                    JsonNode stat = split.path("stat");
                    String team = split.path("team").path("name").asText();
                    String league = split.path("league").path("abbreviation").asText();

                    careerStats.add(new CareerStatRow(
                            String.valueOf(year),
                            team,
                            league,
                            stat.path("gamesPlayed").asInt(),
                            stat.path("avg").asText(),
                            stat.path("homeRuns").asInt(),
                            stat.path("rbi").asInt(),
                            stat.path("ops").asText()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        model.addAttribute("careerStats", careerStats);

        return "kike_hernandez";
    }

    public static class PlayerStats {
        private final int gamesPlayed;
        private final String avg;
        private final int homeRuns;
        private final int rbi;
        private final String ops;

        public PlayerStats(int gamesPlayed, String avg, int homeRuns, int rbi, String ops) {
            this.gamesPlayed = gamesPlayed;
            this.avg = avg;
            this.homeRuns = homeRuns;
            this.rbi = rbi;
            this.ops = ops;
        }

        public int getGamesPlayed() {
            return gamesPlayed;
        }

        public String getAvg() {
            return avg;
        }

        public int getHomeRuns() {
            return homeRuns;
        }

        public int getRbi() {
            return rbi;
        }

        public String getOps() {
            return ops;
        }
    }

    public static class CareerStatRow {
        private final String season;
        private final String team;
        private final String league;
        private final int gamesPlayed;
        private final String avg;
        private final int homeRuns;
        private final int rbi;
        private final String ops;

        public CareerStatRow(String season, String team, String league, int gamesPlayed, String avg, int homeRuns,
                int rbi, String ops) {
            this.season = season;
            this.team = team;
            this.league = league;
            this.gamesPlayed = gamesPlayed;
            this.avg = avg;
            this.homeRuns = homeRuns;
            this.rbi = rbi;
            this.ops = ops;
        }

        public String getSeason() {
            return season;
        }

        public String getTeam() {
            return team;
        }

        public String getLeague() {
            return league;
        }

        public int getGamesPlayed() {
            return gamesPlayed;
        }

        public String getAvg() {
            return avg;
        }

        public int getHomeRuns() {
            return homeRuns;
        }

        public int getRbi() {
            return rbi;
        }

        public String getOps() {
            return ops;
        }
    }
}
