package com.example.dodgersshoheiapp.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/mlb")
@CrossOrigin(origins = "http://localhost:8080") // フロント側許可
public class PostseasonProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/postseason")
    public ResponseEntity<String> getPostseasonData() {
        // 👇 存在しないURLではなくこちらを使用
        String url = "https://statsapi.mlb.com/api/v1/schedule/postseason";
        String result = restTemplate.getForObject(url, String.class);
        return ResponseEntity.ok(result);
    }
}
