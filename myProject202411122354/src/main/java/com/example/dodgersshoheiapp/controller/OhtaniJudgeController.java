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

    private static final String YAMAMOTO_PITCHING_API = "https://statsapi.mlb.com/api/v1/people/808967/stats?stats=season&season=2025&group=pitching";
    private static final String IMANAGA_PITCHING_API = "https://statsapi.mlb.com/api/v1/people/684007/stats?stats=season&season=2025&group=pitching";

    private static final String SKENES_PITCHING_API = "https://statsapi.mlb.com/api/v1/people/694973/stats?stats=season&season=2025&group=pitching";
    private static final String SASAKI_PITCHING_API = "https://statsapi.mlb.com/api/v1/people/808963/stats?stats=season&season=2025&group=pitching";
    
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

    @GetMapping("/api/yamamoto-vs-imanaga/stats")
    public ResponseEntity<Map<String, Object>> getYamamotoVsImanagaStats() {
        RestTemplate restTemplate = new RestTemplate();

        Object yamamotoStats = restTemplate.getForObject(YAMAMOTO_PITCHING_API, Object.class);
        Object imanagaStats = restTemplate.getForObject(IMANAGA_PITCHING_API, Object.class);

        Map<String, Object> result = new HashMap<>();
        result.put("yamamoto", yamamotoStats);
        result.put("imanaga", imanagaStats);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/skenes-vs-sasaki/stats")
    public ResponseEntity<Map<String, Object>> getSkenesVsSasakiStats() {
        RestTemplate restTemplate = new RestTemplate();

        Object skenesStats = restTemplate.getForObject(SKENES_PITCHING_API, Object.class);
        Object sasakiStats = restTemplate.getForObject(SASAKI_PITCHING_API, Object.class);

        Map<String, Object> result = new HashMap<>();
        result.put("skenes", skenesStats);
        result.put("sasaki", sasakiStats);

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
