package com.example.dodgersshoheiapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@RestController
public class OhtaniJudgeController {

    private static final String OHTANI_HITTING_API = "https://statsapi.mlb.com/api/v1/people/660271/stats?stats=season&season=2025&group=hitting";
    private static final String JUDGE_HITTING_API = "https://statsapi.mlb.com/api/v1/people/592450/stats?stats=season&season=2025&group=hitting";

    @GetMapping("/api/ohtani-vs-judge/stats")
    public ResponseEntity<Map<String, Object>> getOhtaniVsJudgeStats() {
        RestTemplate restTemplate = new RestTemplate();

        Object ohtaniStats = restTemplate.getForObject(OHTANI_HITTING_API, Object.class);
        Object judgeStats = restTemplate.getForObject(JUDGE_HITTING_API, Object.class);

        Map<String, Object> result = new HashMap<>();
        result.put("ohtani", ohtaniStats);
        result.put("judge", judgeStats);

        return ResponseEntity.ok(result);
    }
}

@Controller
class OhtaniJudgePageController {
    @GetMapping("/ohtani-vs-judge")
    public String showOhtaniVsJudgePage() {
        return "ohtani_vs_judge";
    }
}
