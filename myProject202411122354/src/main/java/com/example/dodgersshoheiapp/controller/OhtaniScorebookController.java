package com.example.dodgersshoheiapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Controller
public class OhtaniScorebookController {

    @GetMapping("/hogehoge_01")
    public String showSeasonOverview() {
        return "hogehoge_01";
    }

    @GetMapping("/hogehoge_02")
    public String showMonthDetail(
            @RequestParam(name = "month", required = false) Integer month,
            Model model) {

        if (month == null) {
            month = 4;
        }
        model.addAttribute("month", month);

        // ===== ダミー打席 =====
        List<Map<String, String>> atBats = List.of(
                Map.of("no", "1", "pitcher", "yusei kikuchi", "hand", "左", "result", "ショートゴロ"),
                Map.of("no", "2", "pitcher", "yusei kikuchi", "hand", "左", "result", "センターフライ"),
                Map.of("no", "3", "pitcher", "yusei kikuchi", "hand", "左", "result", "Lホームラン"),
                Map.of("no", "4", "pitcher", "yusei kikuchi", "hand", "左", "result", "センターフライ"),
                Map.of("no", "5", "pitcher", "ben joys", "hand", "右", "result", "2Bヒット"));

        model.addAttribute("atBats", atBats);

        // ===== ★ 打撃サマリー（← これが無かった） =====
        model.addAttribute("summary", "5打数 / 2安打");

        // ===== ★ コメント（← これが無かった） =====
        model.addAttribute(
                "comment",
                "昨日と違い、ボールを引き付けてフルスイングできていた。\n" +
                        "3打席目HR。他打席も内容が良く、評価は S");

        // ===== ダミー：ラインスコア（初期値） =====
        Map<String, String> score = Map.of(
                "awayR", "3",
                "awayH", "6",
                "awayE", "1",
                "homeR", "5",
                "homeH", "8",
                "homeE", "0");

        try {
            RestTemplate rest = new RestTemplate();
            String gamePk = "746147";
            String url = "https://statsapi.mlb.com/api/v1/game/" + gamePk + "/linescore";

            Map<String, Object> res = rest.getForObject(url, Map.class);

            if (res != null && res.get("teams") instanceof Map) {
                Map<String, Object> teams = (Map<String, Object>) res.get("teams");

                Map<String, Object> home = (Map<String, Object>) teams.get("home");
                Map<String, Object> away = (Map<String, Object>) teams.get("away");

                Map<String, Object> homeStats = null;
                Map<String, Object> awayStats = null;

                if (home != null && home.get("teamStats") instanceof Map) {
                    homeStats = (Map<String, Object>) ((Map<?, ?>) home.get("teamStats")).get("batting");
                }

                if (away != null && away.get("teamStats") instanceof Map) {
                    awayStats = (Map<String, Object>) ((Map<?, ?>) away.get("teamStats")).get("batting");
                }

                score = Map.of(
                        "awayR", awayStats != null ? String.valueOf(awayStats.getOrDefault("runs", "-")) : "-",
                        "awayH", awayStats != null ? String.valueOf(awayStats.getOrDefault("hits", "-")) : "-",
                        "awayE", awayStats != null ? String.valueOf(awayStats.getOrDefault("errors", "-")) : "-",
                        "homeR", homeStats != null ? String.valueOf(homeStats.getOrDefault("runs", "-")) : "-",
                        "homeH", homeStats != null ? String.valueOf(homeStats.getOrDefault("hits", "-")) : "-",
                        "homeE", homeStats != null ? String.valueOf(homeStats.getOrDefault("errors", "-")) : "-");
            }
        } catch (Exception e) {
            // 失敗してもダミー表示で継続
        }

        model.addAttribute("score", score);

        return "hogehoge_02";
    }
}
