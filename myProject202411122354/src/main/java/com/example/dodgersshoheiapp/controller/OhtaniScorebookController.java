package com.example.dodgersshoheiapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class OhtaniScorebookController {

    /**
     * シーズン俯瞰（hogehoge_01）
     */
    @GetMapping("/hogehoge_01")
    public String showSeasonOverview(Model model) {

        // ===== 4月の日次データ（俯瞰用・DB化前の仮）=====
        List<Map<String, Object>> monthGames = new ArrayList<>();

        monthGames.add(Map.of("date", "04/03", "value", 3));
        monthGames.add(Map.of("date", "04/04", "value", 2));
        monthGames.add(Map.of("date", "04/05", "value", 5));
        monthGames.add(Map.of("date", "04/06", "value", 4));

        monthGames.add(Map.of("date", "04/12", "value", 4));
        monthGames.add(Map.of("date", "04/13", "value", 4));
        monthGames.add(Map.of("date", "04/14", "value", 4));

        monthGames.add(Map.of("date", "04/25", "value", 2));
        monthGames.add(Map.of("date", "04/26", "value", 4));
        monthGames.add(Map.of("date", "04/27", "value", 5));

        // ===== 旬平均 =====
        double earlyAvg = calcAverage(monthGames, List.of("04/03", "04/04", "04/05", "04/06"));
        double midAvg = calcAverage(monthGames, List.of("04/12", "04/13", "04/14"));
        double lateAvg = calcAverage(monthGames, List.of("04/25", "04/26", "04/27"));

        // ===== JS連携 =====
        model.addAttribute("aprilLabels", List.of("4上", "4中", "4下"));
        model.addAttribute("aprilValues", List.of(earlyAvg, midAvg, lateAvg));

        return "hogehoge_01";
    }

    // ===== 平均算出 =====
    private double calcAverage(List<Map<String, Object>> games, List<String> targetDates) {
        return Math.round(
                games.stream()
                        .filter(g -> targetDates.contains(g.get("date")))
                        .mapToInt(g -> (Integer) g.get("value"))
                        .average()
                        .orElse(0.0)
                        * 10.0)
                / 10.0;
    }
}
