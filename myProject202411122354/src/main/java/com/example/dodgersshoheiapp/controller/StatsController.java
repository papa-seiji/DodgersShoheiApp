package com.example.dodgersshoheiapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@RestController
public class StatsController {

    private static final String HITTER_STATS_API_URL = "https://statsapi.mlb.com/api/v1/people/660271/stats?stats=season&season=2024&group=hitting";
    private static final String PITCHER_STATS_API_URL = "https://statsapi.mlb.com/api/v1/people/660271/stats?stats=season&season=2023&group=pitching";

    @GetMapping("/api/stats")
    public ResponseEntity<Object> getStats() {
        RestTemplate restTemplate = new RestTemplate();
        Object hitterStats = restTemplate.getForObject(HITTER_STATS_API_URL, Object.class);
        Object pitcherStats = restTemplate.getForObject(PITCHER_STATS_API_URL, Object.class);

        return ResponseEntity.ok(new Object[] { hitterStats, pitcherStats });
    }
}

@Controller
class StatsPageController {

    @GetMapping("/stats")
    public String statsPage() {
        return "stats"; // templates/stats.html を表示
    }
}
