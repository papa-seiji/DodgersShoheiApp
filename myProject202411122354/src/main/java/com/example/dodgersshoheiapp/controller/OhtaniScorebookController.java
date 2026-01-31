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

        // ===== 3日分の試合データ（ダミー） =====
        List<Map<String, Object>> games = List.of(

                Map.of(
                        "date", "4/12",
                        "result", "○ 5-3",
                        "form", "S",
                        "summary", "5打数 / 2安打",
                        "comment", "3打席目HR。内容が非常に良い。",
                        "score", Map.of(
                                "awayR", "3", "awayH", "6", "awayE", "1",
                                "homeR", "5", "homeH", "8", "homeE", "0"),
                        "atBats", List.of(
                                Map.of("no", "1", "pitcher", "kikuchi", "hand", "左", "result", "ショートゴロ"),
                                Map.of("no", "2", "pitcher", "kikuchi", "hand", "左", "result", "センターフライ"),
                                Map.of("no", "3", "pitcher", "kikuchi", "hand", "左", "result", "HR"))),

                Map.of(
                        "date", "4/13",
                        "result", "● 2-4",
                        "form", "B",
                        "summary", "4打数 / 1安打",
                        "comment", "序盤は合っていなかった。",
                        "score", Map.of(
                                "awayR", "4", "awayH", "7", "awayE", "0",
                                "homeR", "2", "homeH", "5", "homeE", "1"),
                        "atBats", List.of(
                                Map.of("no", "1", "pitcher", "smith", "hand", "右", "result", "三振"),
                                Map.of("no", "2", "pitcher", "smith", "hand", "右", "result", "ヒット"))),

                Map.of(
                        "date", "4/14",
                        "result", "○ 6-1",
                        "form", "A",
                        "summary", "5打数 / 3安打",
                        "comment", "打球が強く、完全復調。",
                        "score", Map.of(
                                "awayR", "1", "awayH", "4", "awayE", "0",
                                "homeR", "6", "homeH", "10", "homeE", "0"),
                        "atBats", List.of(
                                Map.of("no", "1", "pitcher", "lee", "hand", "左", "result", "二塁打"),
                                Map.of("no", "2", "pitcher", "lee", "hand", "左", "result", "ヒット"))));

        model.addAttribute("games", games);

        return "hogehoge_02";
    }
}
