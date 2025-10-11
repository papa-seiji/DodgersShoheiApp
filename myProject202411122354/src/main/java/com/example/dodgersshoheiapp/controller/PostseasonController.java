package com.example.dodgersshoheiapp.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PostseasonController {

    @GetMapping("/postseason")
    public ResponseEntity<?> getPostseasonData() {
        try {
            String url = "https://statsapi.mlb.com/api/v1/schedule?sportId=1&season=2025&gameTypes=W,D,L,F";
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }
}
