package com.example.dodgersshoheiapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import java.util.List;
import java.util.stream.IntStream;
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

        if (month == null)
            month = 4;

        // 打席ダミー
        List<Map<String, String>> atBats = List.of(
                Map.of("no", "1", "pitcher", "yusei kikuchi", "hand", "左", "result", "ショートゴロ"),
                Map.of("no", "2", "pitcher", "yusei kikuchi", "hand", "左", "result", "センターフライ"),
                Map.of("no", "3", "pitcher", "yusei kikuchi", "hand", "左", "result", "Lホームラン"),
                Map.of("no", "4", "pitcher", "yusei kikuchi", "hand", "左", "result", "センターフライ"),
                Map.of("no", "5", "pitcher", "ben joys", "hand", "右", "result", "2Bヒット"));

        model.addAttribute("month", month);
        model.addAttribute("atBats", atBats);
        model.addAttribute("summary", "5打数 / 2安打");
        model.addAttribute("comment",
                "昨日と違い、ボールを引き付けてフルスイングできていた。\n" +
                        "3打席目HR。他打席も内容が良く、評価は S");

        return "hogehoge_02";
    }
}
