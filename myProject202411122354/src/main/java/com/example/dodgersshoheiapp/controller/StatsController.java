package com.example.dodgersshoheiapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam; // ← 追加
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@RestController
public class StatsController {

    // 🔥 固定URLは削除せずそのまま残してOK（使わないだけ）
    private static final String HITTER_STATS_API_URL = "https://statsapi.mlb.com/api/v1/people/660271/stats?stats=season&season=2025&group=hitting";
    private static final String PITCHER_STATS_API_URL = "https://statsapi.mlb.com/api/v1/people/660271/stats?stats=season&season=2025&group=pitching";
    // private static final String HITTER_STATS_API_URL =
    // "https://statsapi.mlb.com/api/v1/people/592450/stats?stats=season&season=2025&group=hitting";

    @GetMapping("/api/stats")
    public ResponseEntity<Object> getStats(
            @RequestParam(defaultValue = "2025") String year // ← 追加
    ) {

        // 🔥 年を動的に組み立て
        String hitterUrl = "https://statsapi.mlb.com/api/v1/people/660271/stats?stats=season&season="
                + year + "&group=hitting";

        String pitcherUrl = "https://statsapi.mlb.com/api/v1/people/660271/stats?stats=season&season="
                + year + "&group=pitching";

        RestTemplate restTemplate = new RestTemplate();

        Object hitterStats = restTemplate.getForObject(hitterUrl, Object.class);
        Object pitcherStats = restTemplate.getForObject(pitcherUrl, Object.class);

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