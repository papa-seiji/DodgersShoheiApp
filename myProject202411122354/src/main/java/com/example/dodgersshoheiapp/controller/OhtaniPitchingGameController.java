package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.dto.LineupResponse;
import com.example.dodgersshoheiapp.model.OhtaniPitchingGame;
import com.example.dodgersshoheiapp.model.OhtaniPitchingGameDetail;
import com.example.dodgersshoheiapp.repository.OhtaniPitchingGameRepository;
import com.example.dodgersshoheiapp.repository.OhtaniPitchingGameDetailRepository;
import com.example.dodgersshoheiapp.service.MlbLineupService; // ★追加
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.*;

@Controller
public class OhtaniPitchingGameController {

    private final OhtaniPitchingGameRepository pitchingGameRepository;
    private final OhtaniPitchingGameDetailRepository detailRepository;
    private final MlbLineupService mlbLineupService; // ★追加

    public OhtaniPitchingGameController(
            OhtaniPitchingGameRepository pitchingGameRepository,
            OhtaniPitchingGameDetailRepository detailRepository,
            MlbLineupService mlbLineupService) { // ★追加
        this.pitchingGameRepository = pitchingGameRepository;
        this.detailRepository = detailRepository;
        this.mlbLineupService = mlbLineupService; // ★追加
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

        OhtaniPitchingGame selectedGame = null;

        if (date != null) {
            LocalDate targetDate = LocalDate.parse(date);

            monthGames = monthGames.stream()
                    .filter(g -> g.getGameDate().equals(targetDate))
                    .toList();

            if (!monthGames.isEmpty()) {
                selectedGame = monthGames.get(0);
            }
        }

        // =========================
        // ★ グラフ用
        // =========================
        List<String> chartLabels = new ArrayList<>();
        List<Integer> chartValues = new ArrayList<>();

        int sum = 0;
        int count = 0;

        for (OhtaniPitchingGame g : monthGames) {
            chartLabels.add(g.getGameDate().toString().substring(5));

            int value = switch (g.getFormValue()) {
                case "S" -> 5;
                case "A" -> 4;
                case "B" -> 3;
                case "C" -> 2;
                default -> 1;
            };

            chartValues.add(value);
            sum += value;
            count++;
        }

        Double monthAverage = (count > 0) ? (double) sum / count : null;

        // =========================
        // ★ detailMap
        // =========================
        Map<Long, OhtaniPitchingGameDetail> detailMap = new HashMap<>();
        for (OhtaniPitchingGame game : monthGames) {
            detailRepository.findByGameIdOrderByIdDesc(game.getId())
                    .stream()
                    .findFirst()
                    .ifPresent(d -> detailMap.put(game.getId(), d));
        }

        // =========================
        // 🔥 ここが今回の本丸
        // =========================
        Map<Long, LineupResponse> linescoreMap = new HashMap<>();

        for (OhtaniPitchingGame game : monthGames) {
            try {
                if (game.getGamePk() != null) { // gamePk がある前提
                    LineupResponse response = mlbLineupService.fetchLineups(game.getGamePk());
                    linescoreMap.put(game.getId(), response);
                }
            } catch (Exception e) {
                // 取得失敗は無視（試合前など）
            }
        }

        // =========================
        // ★ model
        // =========================
        model.addAttribute("month", month);
        model.addAttribute("monthGames", monthGames);
        model.addAttribute("selectedGame", selectedGame);
        model.addAttribute("detailMap", detailMap);
        model.addAttribute("selectedDate", date);
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartValues", chartValues);
        model.addAttribute("monthAverage", monthAverage);

        model.addAttribute("linescoreMap", linescoreMap); // ★追加

        return "hogehoge_04";
    }
}
