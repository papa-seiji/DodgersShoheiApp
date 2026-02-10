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
import java.util.ArrayList;
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

        // =========================
        // ★ hogehoge_02 と同じ絞り込み
        // =========================
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
        // ★ グラフ用（日次・hogehoge_02 と同型）
        // =========================
        List<String> chartLabels = new ArrayList<>();
        List<Integer> chartValues = new ArrayList<>();

        // =========================
        // ★ 月平均評価用（今回の主役）
        // =========================
        int sum = 0;
        int count = 0;

        for (OhtaniPitchingGame g : monthGames) {
            chartLabels.add(g.getGameDate().toString().substring(5)); // MM-dd

            int value = switch (g.getFormValue()) {
                case "S" -> 5;
                case "A" -> 4;
                case "B" -> 3;
                case "C" -> 2;
                default -> 1; // D or null
            };

            chartValues.add(value);

            sum += value;
            count++;
        }

        Double monthAverage = (count > 0) ? (double) sum / count : null;

        // =========================
        // ★ detailMap（カード用）
        // =========================
        Map<Long, OhtaniPitchingGameDetail> detailMap = new HashMap<>();
        for (OhtaniPitchingGame game : monthGames) {
            detailRepository.findByGameIdOrderByIdDesc(game.getId())
                    .stream()
                    .findFirst()
                    .ifPresent(d -> detailMap.put(game.getId(), d));
        }

        model.addAttribute("month", month);
        model.addAttribute("monthGames", monthGames);
        model.addAttribute("selectedGame", selectedGame);
        model.addAttribute("detailMap", detailMap);
        model.addAttribute("selectedDate", date);

        // ★ 日次折れ線用
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartValues", chartValues);

        // ★ 月平均評価（hogehoge_03 用）
        model.addAttribute("monthAverage", monthAverage);

        return "hogehoge_04";
    }
}
