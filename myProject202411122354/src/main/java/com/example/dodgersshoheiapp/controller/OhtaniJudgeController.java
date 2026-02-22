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

    private static final String OHTANI_HITTING_API = "https://statsapi.mlb.com/api/v1/people/660271/stats?stats=season&season=2026&group=hitting";
    private static final String JUDGE_HITTING_API = "https://statsapi.mlb.com/api/v1/people/592450/stats?stats=season&season=2026&group=hitting";

    private static final String TROUT_HITTING_API = "https://statsapi.mlb.com/api/v1/people/545361/stats?stats=season&season=2026&group=hitting";
    private static final String EDOMAN_HITTING_API = "https://statsapi.mlb.com/api/v1/people/669242/stats?stats=season&season=2026&group=hitting";

    private static final String SEIYA_HITTING_API = "https://statsapi.mlb.com/api/v1/people/673548/stats?stats=season&season=2026&group=hitting";
    private static final String BETTS_HITTING_API = "https://statsapi.mlb.com/api/v1/people/605141/stats?stats=season&season=2026&group=hitting";

    private static final String YAMAMOTO_PITCHING_API = "https://statsapi.mlb.com/api/v1/people/808967/stats?stats=season&season=2026&group=pitching";
    private static final String IMANAGA_PITCHING_API = "https://statsapi.mlb.com/api/v1/people/684007/stats?stats=season&season=2026&group=pitching";

    private static final String SUGANO_PITCHING_API = "https://statsapi.mlb.com/api/v1/people/608372/stats?stats=season&season=2026&group=pitching";
    private static final String SASAKI_PITCHING_API = "https://statsapi.mlb.com/api/v1/people/808963/stats?stats=season&season=2026&group=pitching";

    private static final String DARVISH_PITCHING_API = "https://statsapi.mlb.com/api/v1/people/506433/stats?stats=season&season=2026&group=pitching";
    private static final String MISIOROWSKI_PITCHING_API = "https://statsapi.mlb.com/api/v1/people/694819/stats?stats=season&season=2026&group=pitching";

    private static final String SCHWARBER_HITTING_API = "https://statsapi.mlb.com/api/v1/people/656941/stats?stats=season&season=2026&group=hitting";
    private static final String TEOSCAR_HITTING_API = "https://statsapi.mlb.com/api/v1/people/606192/stats?stats=season&season=2026&group=hitting";

    private static final String RALEIGH_HITTING_API = "https://statsapi.mlb.com/api/v1/people/663728/stats?stats=season&season=2026&group=hitting";
    private static final String YOSHIDA_HITTING_API = "https://statsapi.mlb.com/api/v1/people/807799/stats?stats=season&season=2026&group=hitting";

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

    @GetMapping("/api/trout-vs-edoman/stats")
    public ResponseEntity<Map<String, Object>> getTroutVsEdomanStats() {
        RestTemplate restTemplate = new RestTemplate();

        Object troutStats = restTemplate.getForObject(TROUT_HITTING_API, Object.class);
        Object edomanStats = restTemplate.getForObject(EDOMAN_HITTING_API, Object.class);

        Map<String, Object> result = new HashMap<>();
        result.put("trout", troutStats);
        result.put("edoman", edomanStats);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/seiya-vs-betts/stats")
    public ResponseEntity<Map<String, Object>> getSeiyaVsBettsStats() {
        RestTemplate restTemplate = new RestTemplate();

        Object seiyaStats = restTemplate.getForObject(SEIYA_HITTING_API, Object.class);
        Object bettsStats = restTemplate.getForObject(BETTS_HITTING_API, Object.class);

        Map<String, Object> result = new HashMap<>();
        result.put("seiya", seiyaStats);
        result.put("betts", bettsStats);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/schwarber-vs-teoscar/stats")
    public ResponseEntity<Map<String, Object>> getSchwarberVsTeoscarStats() {
        RestTemplate restTemplate = new RestTemplate();

        Object schwarberStats = restTemplate.getForObject(SCHWARBER_HITTING_API, Object.class);
        Object teoscarStats = restTemplate.getForObject(TEOSCAR_HITTING_API, Object.class);

        Map<String, Object> result = new HashMap<>();
        result.put("schwarber", schwarberStats);
        result.put("teoscar", teoscarStats);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/raleigh-vs-yoshida/stats")
    public ResponseEntity<Map<String, Object>> getRaleighVsXxxxxxStats() {
        RestTemplate restTemplate = new RestTemplate();

        Object raleighStats = restTemplate.getForObject(RALEIGH_HITTING_API, Object.class);
        Object yoshidaStats = restTemplate.getForObject(YOSHIDA_HITTING_API, Object.class);

        Map<String, Object> result = new HashMap<>();
        result.put("raleigh", raleighStats);
        result.put("yoshida", yoshidaStats);

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

    @GetMapping("/api/sugano-vs-sasaki/stats")
    public ResponseEntity<Map<String, Object>> getSkenesVsSasakiStats() {
        RestTemplate restTemplate = new RestTemplate();

        Object suganoStats = restTemplate.getForObject(SUGANO_PITCHING_API, Object.class);
        Object sasakiStats = restTemplate.getForObject(SASAKI_PITCHING_API, Object.class);

        Map<String, Object> result = new HashMap<>();
        result.put("sugano", suganoStats);
        result.put("sasaki", sasakiStats);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/darvish-vs-misiorowski/stats")
    public ResponseEntity<Map<String, Object>> getDarvishVsMisiorowskiStats() {
        RestTemplate restTemplate = new RestTemplate();

        Object darvishStats = restTemplate.getForObject(DARVISH_PITCHING_API, Object.class);
        Object misiorowskiStats = restTemplate.getForObject(MISIOROWSKI_PITCHING_API, Object.class);

        Map<String, Object> result = new HashMap<>();
        result.put("darvish", darvishStats);
        result.put("misiorowski", misiorowskiStats);

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
