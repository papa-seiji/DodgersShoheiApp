package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.OhtaniGame;
import com.example.dodgersshoheiapp.model.OhtaniGameDetail;
import com.example.dodgersshoheiapp.repository.OhtaniGameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class OhtaniGameController {

    private final OhtaniGameRepository repository;

    @GetMapping("/hogehoge_02")
    public String showMonthlyGames(
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "date", required = false) String date,
            Model model) {

        int year = 2026;
        if (month == null) {
            month = 4;
        }

        model.addAttribute("month", month);

        // ===== 月別 試合一覧（★ 常に使う）=====
        List<OhtaniGame> monthGames = repository.findGamesByMonth(year, month);
        model.addAttribute("monthGames", monthGames); // ← ★これが無いと一覧が出ない

        // ===== グラフ用 =====
        List<String> chartLabels = new ArrayList<>();
        List<Integer> chartValues = new ArrayList<>();

        for (OhtaniGame g : monthGames) {
            chartLabels.add(g.getGameDate().toString().substring(5)); // MM-dd

            int graphValue = switch (g.getFormValue()) {
                case 2 -> 5; // S
                case 1 -> 4; // A
                case 0 -> 3; // B
                case -1 -> 2; // C
                default -> 1; // D
            };

            chartValues.add(graphValue);
        }

        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartValues", chartValues);

        // ===== 詳細表示用（切替）=====
        List<OhtaniGame> targetGames = new ArrayList<>();

        if (date != null) {
            // 日付指定 → その試合だけ
            LocalDate targetDate = LocalDate.parse(date);

            // ⭐️【ここ】に1行追加（この直後）
            model.addAttribute("selectedDate", targetDate);

            for (OhtaniGame g : monthGames) {
                if (g.getGameDate().equals(targetDate)) {
                    targetGames.add(g);
                    break;
                }
            }
        } else {
            // 未指定 → 全試合
            targetGames = monthGames;
        }

        // ===== 打席詳細結合 =====
        for (OhtaniGame game : targetGames) {
            List<OhtaniGameDetail> details = repository.findDetailsByGameId(game.getId());
            game.setDetails(details);
        }

        model.addAttribute("games", targetGames);

        return "hogehoge_02";
    }
}
