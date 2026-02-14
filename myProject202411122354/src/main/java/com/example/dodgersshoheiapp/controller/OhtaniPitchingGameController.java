package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.dto.LineupResponse;
import com.example.dodgersshoheiapp.model.OhtaniPitchingGame;
import com.example.dodgersshoheiapp.model.OhtaniPitchingGameDetail;
import com.example.dodgersshoheiapp.repository.OhtaniPitchingGameRepository;
import com.example.dodgersshoheiapp.repository.OhtaniPitchingGameDetailRepository;
import com.example.dodgersshoheiapp.service.MlbLineupService;
import com.example.dodgersshoheiapp.service.MLBGameService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class OhtaniPitchingGameController {

    private final OhtaniPitchingGameRepository pitchingGameRepository;
    private final OhtaniPitchingGameDetailRepository detailRepository;
    private final MlbLineupService mlbLineupService;
    private final MLBGameService mlbGameService;

    public OhtaniPitchingGameController(
            OhtaniPitchingGameRepository pitchingGameRepository,
            OhtaniPitchingGameDetailRepository detailRepository,
            MlbLineupService mlbLineupService,
            MLBGameService mlbGameService) {

        this.pitchingGameRepository = pitchingGameRepository;
        this.detailRepository = detailRepository;
        this.mlbLineupService = mlbLineupService;
        this.mlbGameService = mlbGameService;
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

        // ==============================
        // 一覧（昇順固定）
        // ==============================
        List<OhtaniPitchingGame> listGames = pitchingGameRepository
                .findByGameDateBetweenOrderByGameDateAsc(startDate, endDate);

        // ==============================
        // gamePk 自動取得
        // ==============================
        for (OhtaniPitchingGame game : listGames) {

            if (game.getGamePk() == null) {
                try {
                    Long gamePk = mlbGameService.findGamePkByDate(
                            game.getGameDate().toString());

                    if (gamePk != null) {
                        game.setGamePk(gamePk);
                        pitchingGameRepository.save(game);
                    }

                } catch (Exception e) {
                    System.out.println("gamePk取得失敗: " + e.getMessage());
                }
            }
        }

        // ==============================
        // selectedGame（これが重要）
        // ==============================
        OhtaniPitchingGame selectedGame = null;

        if (date != null) {
            LocalDate targetDate = LocalDate.parse(date);

            selectedGame = listGames.stream()
                    .filter(g -> g.getGameDate().equals(targetDate))
                    .findFirst()
                    .orElse(null);
        }

        // ==============================
        // グラフ
        // ==============================
        List<String> chartLabels = new ArrayList<>();
        List<Integer> chartValues = new ArrayList<>();

        int sum = 0;
        int count = 0;

        for (OhtaniPitchingGame g : listGames) {

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

        // ==============================
        // detailMap
        // ==============================
        Map<Long, OhtaniPitchingGameDetail> detailMap = new HashMap<>();

        for (OhtaniPitchingGame game : listGames) {
            detailRepository.findByGameIdOrderByIdDesc(game.getId())
                    .stream()
                    .findFirst()
                    .ifPresent(d -> detailMap.put(game.getId(), d));
        }

        // ==============================
        // linescore
        // ==============================
        Map<Long, LineupResponse> linescoreMap = new HashMap<>();

        for (OhtaniPitchingGame game : listGames) {
            try {
                if (game.getGamePk() != null) {
                    LineupResponse response = mlbLineupService.fetchLineups(game.getGamePk());
                    linescoreMap.put(game.getId(), response);
                }
            } catch (Exception ignored) {
            }
        }

        // ==============================
        // model
        // ==============================
        model.addAttribute("month", month);
        model.addAttribute("monthGames", listGames);
        model.addAttribute("selectedGame", selectedGame);
        model.addAttribute("selectedDate", date); // ←重要
        model.addAttribute("detailMap", detailMap);
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartValues", chartValues);
        model.addAttribute("monthAverage", monthAverage);
        model.addAttribute("linescoreMap", linescoreMap);

        return "hogehoge_04";
    }
}
