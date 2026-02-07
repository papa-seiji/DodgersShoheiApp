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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            Model model) {

        int year = 2026;
        if (month == null) {
            month = 4;
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // =========================
        // ① 月内の全登板試合
        // =========================
        List<OhtaniPitchingGame> monthGames = pitchingGameRepository.findByGameDateBetween(startDate, endDate);

        // =========================
        // ② 試合ID → Detail のMap
        // =========================
        Map<Long, OhtaniPitchingGameDetail> detailMap = new HashMap<>();

        for (OhtaniPitchingGame game : monthGames) {
            detailRepository
                    .findByGameIdOrderByIdDesc(game.getId())
                    .stream()
                    .findFirst()
                    .ifPresent(detail -> detailMap.put(game.getId(), detail));
        }

        // =========================
        // ③ View へ渡す
        // =========================
        model.addAttribute("month", month);
        model.addAttribute("monthGames", monthGames);
        model.addAttribute("detailMap", detailMap);

        return "hogehoge_04";
    }
}
