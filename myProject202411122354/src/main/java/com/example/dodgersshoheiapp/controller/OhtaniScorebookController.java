package com.example.dodgersshoheiapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
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
            @RequestParam(name = "date", required = false) String date,
            Model model) {

        if (month == null) {
            month = 4;
        }
        model.addAttribute("month", month);

        // ===== 4月一覧・グラフ共通データ（valueが唯一の評価軸）=====
        List<Map<String, Object>> monthGames = new ArrayList<>();

        monthGames.add(new HashMap<>(Map.of("date", "04/03", "result", "○ 4-2", "value", 3))); // B
        monthGames.add(new HashMap<>(Map.of("date", "04/04", "result", "● 2-5", "value", 2))); // C
        monthGames.add(new HashMap<>(Map.of("date", "04/05", "result", "○ 6-1", "value", 5))); // S
        monthGames.add(new HashMap<>(Map.of("date", "04/06", "result", "○ 5-3", "value", 4))); // A

        monthGames.add(new HashMap<>(Map.of("date", "04/12", "result", "○ 5-3", "value", 4))); // A
        monthGames.add(new HashMap<>(Map.of("date", "04/13", "result", "○ 5-3", "value", 4))); // A
        monthGames.add(new HashMap<>(Map.of("date", "04/14", "result", "○ 5-3", "value", 4))); // A

        monthGames.add(new HashMap<>(Map.of("date", "04/25", "result", "● 1-4", "value", 2))); // C
        monthGames.add(new HashMap<>(Map.of("date", "04/26", "result", "○ 7-2", "value", 4))); // A
        monthGames.add(new HashMap<>(Map.of("date", "04/27", "result", "○ 8-0", "value", 5))); // S

        // ===== グラフ用データ（JS連携用）=====
        List<String> chartLabels = new ArrayList<>();
        List<Integer> chartValues = new ArrayList<>();

        for (Map<String, Object> g : monthGames) {
            chartLabels.add((String) g.get("date"));
            chartValues.add((Integer) g.get("value"));
        }

        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartValues", chartValues);

        // ===== 詳細カード用 =====
        List<Map<String, Object>> detailGames = new ArrayList<>();

        for (Map<String, Object> g : monthGames) {

            Map<String, String> score = Map.of(
                    "awayR", "-",
                    "awayH", "-",
                    "awayE", "-",
                    "homeR", "-",
                    "homeH", "-",
                    "homeE", "-");

            List<Map<String, String>> atBats = List.of(
                    Map.of("no", "1", "pitcher", "yusei kikuchi", "hand", "左", "result", "ショートゴロ"),
                    Map.of("no", "2", "pitcher", "yusei kikuchi", "hand", "左", "result", "センターフライ"),
                    Map.of("no", "3", "pitcher", "yusei kikuchi", "hand", "左", "result", "Lホームラン"));

            detailGames.add(new HashMap<>(Map.of(
                    "date", g.get("date"),
                    "result", g.get("result"),
                    "value", g.get("value"), // ★評価は value のみ
                    "score", score,
                    "summary", "5打数 / 2安打",
                    "comment", "引き付けて強いスイング。内容が非常に良い。",
                    "atBats", atBats)));
        }

        // ===== 日付指定があれば単試合 =====
        if (date != null) {
            detailGames = detailGames.stream()
                    .filter(g -> date.equals(g.get("date")))
                    .toList();
            model.addAttribute("singleView", true);
        } else {
            model.addAttribute("singleView", false);
        }

        model.addAttribute("monthGames", monthGames);
        model.addAttribute("games", detailGames);

        return "hogehoge_02";
    }
}
