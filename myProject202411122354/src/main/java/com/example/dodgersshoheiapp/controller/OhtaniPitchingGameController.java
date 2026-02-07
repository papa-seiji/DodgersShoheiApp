package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.OhtaniPitchingGame;
import com.example.dodgersshoheiapp.model.OhtaniPitchingGameDetail;
import com.example.dodgersshoheiapp.repository.OhtaniPitchingGameRepository;
import com.example.dodgersshoheiapp.repository.OhtaniPitchingGameDetailRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class OhtaniPitchingGameController {

    private final OhtaniPitchingGameRepository pitchingGameRepository;
    private final OhtaniPitchingGameDetailRepository detailRepository;

    public OhtaniPitchingGameController(
            OhtaniPitchingGameRepository pitchingGameRepository,
            OhtaniPitchingGameDetailRepository detailRepository) {
        this.pitchingGameRepository = pitchingGameRepository;
        this.detailRepository = detailRepository;
    }

    @GetMapping("/hogehoge_04")
    public String showPitchingMonth(
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "date", required = false) String date,
            Model model) {

        int year = 2026;
        if (month == null) {
            month = 4;
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<OhtaniPitchingGame> monthGames = pitchingGameRepository.findByGameDateBetween(startDate, endDate);

        // =========================
        // ★ BATTING側と同じ絞り込みロジック
        // =========================
        OhtaniPitchingGame selectedGame = null;

        if (date != null) {
            LocalDate targetDate = LocalDate.parse(date);

            monthGames = monthGames.stream()
                    .filter(g -> g.getGameDate().equals(targetDate))
                    .toList();

            if (!monthGames.isEmpty()) {
                selectedGame = monthGames.get(0);
            }
        } else {
            // 初期表示：月の先頭試合をデフォルト詳細にする
            if (!monthGames.isEmpty()) {
                selectedGame = monthGames.get(0);
            }
        }

        // =========================
        // ★ イニング詳細（1試合 = 1レコード）
        // =========================
        OhtaniPitchingGameDetail detail = null;
        if (selectedGame != null) {
            detail = detailRepository
                    .findByGameIdOrderByIdDesc(selectedGame.getId())
                    .stream()
                    .findFirst()
                    .orElse(null);
        }

        model.addAttribute("month", month);
        model.addAttribute("monthGames", monthGames);
        model.addAttribute("selectedGame", selectedGame);
        model.addAttribute("detail", detail); // ★ここが重要

        return "hogehoge_04";

    }
}
