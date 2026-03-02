package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.dto.LineupResponse;
import com.example.dodgersshoheiapp.model.OhtaniGame;
import com.example.dodgersshoheiapp.model.OhtaniGameDetail;
import com.example.dodgersshoheiapp.repository.OhtaniGameRepository;
import com.example.dodgersshoheiapp.repository.OhtaniPitchingGameRepository;
import com.example.dodgersshoheiapp.service.MLBGameService;
import com.example.dodgersshoheiapp.service.MlbLineupService;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class OhtaniGameController {

    private final OhtaniGameRepository repository;
    private final OhtaniPitchingGameRepository pitchingGameRepository;
    private final MlbLineupService lineupService; // ★追加
    private final MLBGameService mlbGameService; // ★追加

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

            System.out.println("DEBUG gameDate=" + game.getGameDate()
                    + " gamePk=" + game.getGamePk());

            try {

                // =====================================================
                // 🧠 STEP 2 — gamePk 自動取得（NULL時のみ）
                // =====================================================
                if (game.getGamePk() == null) {

                    final int DODGERS_TEAM_ID = 119;

                    var autoGamePkOpt = lineupService.findGamePkByDate(DODGERS_TEAM_ID, game.getGameDate());

                    if (autoGamePkOpt.isPresent()) {

                        long autoGamePk = autoGamePkOpt.getAsLong();

                        System.out.println("AUTO gamePk found = " + autoGamePk);

                        game.setGamePk(autoGamePk);

                        // DB保存
                        repository.updateGamePk(game.getId(), autoGamePk);
                    }
                }

                // =====================================================
                // 🔥 Linescore + H/E 注入
                // =====================================================
                if (game.getGamePk() != null) {

                    LineupResponse res = lineupService.fetchLineups(game.getGamePk());

                    if (res != null) {

                        // ===== 🔥 Team名 注入（record対応） =====
                        if (res.home() != null) {
                            game.setHomeTeamName(res.home().teamName());
                        }

                        if (res.away() != null) {
                            game.setAwayTeamName(res.away().teamName());
                        }

                        // ===== 各回 Runs =====
                        game.setHomeRunsByInning(res.homeRunsByInning());
                        game.setAwayRunsByInning(res.awayRunsByInning());

                        // ===== Runs合計 =====
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

                        // ===== H / E =====
                        game.setHomeHits(res.homeHits());
                        game.setAwayHits(res.awayHits());
                        game.setHomeErrors(res.homeErrors());
                        game.setAwayErrors(res.awayErrors());

                        System.out.println("DEBUG linescore + HE injected for gamePk="
                                + game.getGamePk());
                    }
                }

            } catch (Exception e) {
                System.out.println("Game processing failed for date="
                        + game.getGameDate());
                e.printStackTrace();
            }
        }

        model.addAttribute("games", targetGames);

        // ===============================
        // 🔥 DEBUG MOCK Shohei HR 表示確認用
        // ===============================
        // {
        // List<Map<String, Object>> mock = new ArrayList<>();
        //
        // Map<String, Object> testHr = new HashMap<>();
        //
        // testHr.put("hitter", "Shohei Ohtani");
        // testHr.put("launchSpeed", 120.1);
        // testHr.put("launchAngle", 32);
        // testHr.put("totalDistance", 398);
        // testHr.put("coordX", 80);
        // testHr.put("coordY", 120);
        //
        // mock.add(testHr);
        //
        // model.addAttribute("debugShoheiHRs", mock);
        //
        // return "hogehoge_02";
        // }

        // ===============================
        // 🔥 Shohei HR 実データ注入（試合単位保持）
        // ===============================

        for (OhtaniGame game : targetGames) {

            // 念のため初期化（前回値残り防止）
            game.setShoheiHRs(null);

            if (game.getGamePk() != null) {

                List<Map<String, Object>> hrs = mlbGameService.getShoheiHomeRuns(game.getGamePk());

                if (hrs != null && !hrs.isEmpty()) {
                    game.setShoheiHRs(hrs);
                }
            }
        }

        model.addAttribute("games", targetGames);

        return "hogehoge_02";

        // ===============================
        // 🔥 Shohei HR 実データ注入（テスト用固定gamePk）フィールド描画確認用ロジック
        // ===============================

        // Long testGamePk = 813031L; // ← 去年のShohei HRがある試合のgamePkに変更

        // List<Map<String, Object>> shoheiHRs =
        // mlbGameService.getShoheiHomeRuns(testGamePk);

        // model.addAttribute("debugShoheiHRs", shoheiHRs);

        // return "hogehoge_02";
    }
}
