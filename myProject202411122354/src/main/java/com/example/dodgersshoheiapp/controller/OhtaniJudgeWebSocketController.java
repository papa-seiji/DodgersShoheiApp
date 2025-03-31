package com.example.dodgersshoheiapp.controller;

import java.util.Map;
import java.util.HashMap;

import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

@Controller
public class OhtaniJudgeWebSocketController {

    @MessageMapping("/stats/request") // フロントが送るパス（/app/stats/request）
    @SendTo("/topic/stats") // サーバーからクライアントへ配信
    public Map<String, Object> sendStats() {
        RestTemplate restTemplate = new RestTemplate();

        Object ohtani = restTemplate.getForObject("https://statsapi.mlb.com/api/v1/people/660271/stats?stats=season&season=2025&group=hitting", Object.class);
        Object judge = restTemplate.getForObject("https://statsapi.mlb.com/api/v1/people/592450/stats?stats=season&season=2025&group=hitting", Object.class);

        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put("ohtani", ohtani);
        statsMap.put("judge", judge);

        return statsMap;
    }
}
