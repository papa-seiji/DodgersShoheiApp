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

                // 打撃成績（トラウト・エドマン）
                Object trout = restTemplate.getForObject(
                                "https://statsapi.mlb.com/api/v1/people/545361/stats?stats=season&season=2025&group=hitting",
                                Object.class);
                Object edoman = restTemplate.getForObject(
                                "https://statsapi.mlb.com/api/v1/people/669242/stats?stats=season&season=2025&group=hitting",
                                Object.class);

                // 打撃成績（誠也・ベッツ）
                Object seiya = restTemplate.getForObject(
                                "https://statsapi.mlb.com/api/v1/people/673548/stats?stats=season&season=2025&group=hitting",
                                Object.class);
                Object betts = restTemplate.getForObject(
                                "https://statsapi.mlb.com/api/v1/people/605141/stats?stats=season&season=2025&group=hitting",
                                Object.class);

                // 打撃成績（シュワバー・テオスカー）
                Object schwarber = restTemplate.getForObject(
                                "https://statsapi.mlb.com/api/v1/people/656941/stats?stats=season&season=2025&group=hitting",
                                Object.class);
                Object teoscar = restTemplate.getForObject(
                                "https://statsapi.mlb.com/api/v1/people/606192/stats?stats=season&season=2025&group=hitting",

                                Object.class);
                // 打撃成績（ローリー・xxxxxx）
                Object raleigh = restTemplate.getForObject(
                                "https://statsapi.mlb.com/api/v1/people/663728/stats?stats=season&season=2025&group=hitting",
                                Object.class);
                // Object xxxxxx = restTemplate.getForObject(
                // "https://statsapi.mlb.com/api/v1/people/xxxxxx/stats?stats=season&season=2025&group=hitting",
                // Object.class);

                // 投手成績（山本由伸・今永昇太）
                Object yamamoto = restTemplate.getForObject(
                                "https://statsapi.mlb.com/api/v1/people/808967/stats?stats=season&season=2025&group=pitching",
                                Object.class);
                Object imanaga = restTemplate.getForObject(
                                "https://statsapi.mlb.com/api/v1/people/684007/stats?stats=season&season=2025&group=pitching",
                                Object.class);

                // 投手成績（菅野智之・佐々木朗希）
                Object sugano = restTemplate.getForObject(
                                "https://statsapi.mlb.com/api/v1/people/608372/stats?stats=season&season=2025&group=pitching",
                                Object.class);
                Object sasaki = restTemplate.getForObject(
                                "https://statsapi.mlb.com/api/v1/people/808963/stats?stats=season&season=2025&group=pitching",
                                Object.class);

                // まとめて返す
                Map<String, Object> statsMap = new HashMap<>();
                statsMap.put("ohtani", ohtani);
                statsMap.put("judge", judge);
                statsMap.put("trout", trout);
                statsMap.put("edoman", edoman);
                statsMap.put("seiya", seiya);
                statsMap.put("betts", betts);
                statsMap.put("schwarber", schwarber);
                statsMap.put("teoscar", teoscar);
                statsMap.put("raleigh", raleigh);

                statsMap.put("yamamoto", yamamoto);
                statsMap.put("imanaga", imanaga);
                statsMap.put("sugano", sugano);
                statsMap.put("sasaki", sasaki);

                return statsMap;
        }
}
