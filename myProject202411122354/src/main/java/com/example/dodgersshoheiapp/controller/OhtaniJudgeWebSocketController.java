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

        // 打撃成績（大谷・ジャッジ）
        Object ohtani = restTemplate.getForObject(
            "https://statsapi.mlb.com/api/v1/people/660271/stats?stats=season&season=2025&group=hitting",
            Object.class);
        Object judge = restTemplate.getForObject(
            "https://statsapi.mlb.com/api/v1/people/592450/stats?stats=season&season=2025&group=hitting",
            Object.class);

        // 投手成績（山本由伸・今永昇太）
        Object yamamoto = restTemplate.getForObject(
            "https://statsapi.mlb.com/api/v1/people/808967/stats?stats=season&season=2025&group=pitching",
            Object.class);
        Object imanaga = restTemplate.getForObject(
            "https://statsapi.mlb.com/api/v1/people/684007/stats?stats=season&season=2025&group=pitching",
            Object.class);

        // 投手成績（ポール・スキーンズ・佐々木朗希）
        Object skenes = restTemplate.getForObject(
            "https://statsapi.mlb.com/api/v1/people/694973/stats?stats=season&season=2025&group=pitching",
            Object.class);
        Object sasaki = restTemplate.getForObject(
            "https://statsapi.mlb.com/api/v1/people/808963/stats?stats=season&season=2025&group=pitching",
            Object.class);

        // まとめて返す
        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put("ohtani", ohtani);
        statsMap.put("judge", judge);
        statsMap.put("yamamoto", yamamoto);
        statsMap.put("imanaga", imanaga);
        statsMap.put("skenes", skenes);
        statsMap.put("sasaki", sasaki);

        return statsMap;
    }
}
