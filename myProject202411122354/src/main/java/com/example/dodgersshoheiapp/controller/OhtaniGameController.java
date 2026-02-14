package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.dto.LineupResponse;
import com.example.dodgersshoheiapp.model.OhtaniGame;
import com.example.dodgersshoheiapp.model.OhtaniGameDetail;
import com.example.dodgersshoheiapp.repository.OhtaniGameRepository;
import com.example.dodgersshoheiapp.repository.OhtaniPitchingGameRepository;
import com.example.dodgersshoheiapp.service.MlbLineupService;

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
    private final OhtaniPitchingGameRepository pitchingGameRepository;
    private final MlbLineupService lineupService; // ★追加

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

        // ===== 月別 試合一覧 =====
        List<OhtaniGame> monthGames = repository.findGamesByMonth(year, month);
        model.addAttribute("monthGames", monthGames);

        // ===== グラフ用 =====
        List<String> chartLabels = new ArrayList<>();
        List<Integer> chartValues = new ArrayList<>();

        for (OhtaniGame g : monthGames) {
            chartLabels.add(g.getGameDate().toString().substring(5));

            int graphValue = switch (g.getFormValue()) {
                case 2 -> 5;
                case 1 -> 4;
                case 0 -> 3;
                case -1 -> 2;
                default -> 1;
            };

            chartValues.add(graphValue);
        }

        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartValues", chartValues);

        // ===== 詳細対象決定 =====
        List<OhtaniGame> targetGames = new ArrayList<>();

        if (date != null) {
            LocalDate targetDate = LocalDate.parse(date);
            model.addAttribute("selectedDate", targetDate);

            for (OhtaniGame g : monthGames) {
                if (g.getGameDate().equals(targetDate)) {
                    targetGames.add(g);
                    break;
                }
            }
        } else {
            targetGames = monthGames;
        }

        // ===== 打席詳細 + Linescore 注入 =====
        for (OhtaniGame game : targetGames) {

            // ===== 打席詳細 =====
            List<OhtaniGameDetail> details = repository.findDetailsByGameId(game.getId());
            game.setDetails(details);

            // ===== 🔥 Linescore + H/E 注入 =====
            System.out.println("DEBUG gameDate=" + game.getGameDate()
                    + " gamePk=" + game.getGamePk());

            if (game.getGamePk() != null) {
                try {

                    LineupResponse res = lineupService.fetchLineups(game.getGamePk());

                    if (res != null) {

                        // ===== 各回Runs =====
                        game.setHomeRunsByInning(res.homeRunsByInning());
                        game.setAwayRunsByInning(res.awayRunsByInning());

                        // ===== ★ 合計RunsをControllerで計算 =====
                        if (res.homeRunsByInning() != null) {
                            int homeTotal = res.homeRunsByInning()
                                    .stream()
                                    .mapToInt(Integer::intValue)
                                    .sum();
                            game.setHomeTotalRuns(homeTotal);
                        }

                        if (res.awayRunsByInning() != null) {
                            int awayTotal = res.awayRunsByInning()
                                    .stream()
                                    .mapToInt(Integer::intValue)
                                    .sum();
                            game.setAwayTotalRuns(awayTotal);
                        }

                        // ===== 🔥 H / E 注入 =====
                        game.setHomeHits(res.homeHits());
                        game.setAwayHits(res.awayHits());
                        game.setHomeErrors(res.homeErrors());
                        game.setAwayErrors(res.awayErrors());

                        System.out.println("DEBUG linescore + HE injected for gamePk="
                                + game.getGamePk());

                    } else {
                        System.out.println("DEBUG LineupResponse is null for gamePk="
                                + game.getGamePk());
                    }

                } catch (Exception e) {
                    System.out.println("Linescore fetch failed for gamePk="
                            + game.getGamePk());
                    e.printStackTrace();
                }

            } else {
                System.out.println("DEBUG gamePk is NULL for date="
                        + game.getGameDate());
            }
        }
        model.addAttribute("games", targetGames);

        return "hogehoge_02";
    }
}
