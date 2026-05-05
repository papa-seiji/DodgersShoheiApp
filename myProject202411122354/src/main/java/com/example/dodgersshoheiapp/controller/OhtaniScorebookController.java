package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.OhtaniGame;
import com.example.dodgersshoheiapp.model.OhtaniPitchingGame;
import com.example.dodgersshoheiapp.repository.OhtaniGameRepository;
import com.example.dodgersshoheiapp.repository.OhtaniPitchingGameRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.dodgersshoheiapp.service.MLBGameService; // ←追加

import lombok.RequiredArgsConstructor;

import java.util.Map;// ←追加

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
public class OhtaniScorebookController {

    private final AdminNewsController adminNewsController;
    private final OhtaniGameRepository gameRepository;
    private final OhtaniPitchingGameRepository pitchingGameRepository;
    private final MLBGameService mlbGameService; // ←追加

    public OhtaniScorebookController(
            OhtaniGameRepository gameRepository,
            OhtaniPitchingGameRepository pitchingGameRepository, MLBGameService mlbGameService,
            AdminNewsController adminNewsController) { // ←追加
        this.gameRepository = gameRepository;
        this.pitchingGameRepository = pitchingGameRepository;
        this.mlbGameService = mlbGameService; // ←追加
        this.adminNewsController = adminNewsController;
    }

    /**
     * ============================
     * シーズン俯瞰（hogehoge_01）
     * ============================
     */
    @GetMapping("/hogehoge_01")
    public String showSeasonOverview(Model model) {

        // ===== BATTING（直近）=====
        LocalDate latestGameDate = gameRepository.findLatestGameDate();
        OhtaniGame latestGame = gameRepository.findLatestGame();

        model.addAttribute("latestGameDate", latestGame.getGameDate());
        model.addAttribute("latestFormRank",
                convertFormRank(latestGame.getFormValue()));
        model.addAttribute("latestFormEmoji",
                convertFormEmoji(latestGame.getFormValue()));
        model.addAttribute("latestComment", latestGame.getComment());

        int latestMonth = latestGameDate.getMonthValue();

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        // ===== BATTING（旬平均）=====
        for (int m = 3; m <= Math.min(latestMonth, 11); m++) {
            labels.add(m + "月上旬");
            values.add(avgForm(2026, m, 1, 10));

            labels.add(m + "月中旬");
            values.add(avgForm(2026, m, 11, 20));

            labels.add(m + "月下旬");
            values.add(avgForm(
                    2026, m, 21,
                    LocalDate.of(2026, m, 1).lengthOfMonth()));
        }

        model.addAttribute("labels", labels);
        model.addAttribute("values", values);

        // ===== PITCHING（月平均・3〜11月）=====
        List<String> pitchingMonthLabels = new ArrayList<>();
        List<Double> pitchingMonthAverages = new ArrayList<>();

        int year = 2026;

        for (int m = 3; m <= 11; m++) {

            LocalDate start = LocalDate.of(year, m, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            List<OhtaniPitchingGame> games = pitchingGameRepository.findByGameDateBetween(start, end);

            pitchingMonthLabels.add(m + "月");

            if (games.isEmpty()) {
                pitchingMonthAverages.add(null);
                continue;
            }

            int sum = 0;
            int count = 0;

            for (OhtaniPitchingGame g : games) {
                int value = switch (g.getFormValue()) {
                    case "S" -> 5;
                    case "A" -> 4;
                    case "B" -> 3;
                    case "C" -> 2;
                    default -> 1;
                };
                sum += value;
                count++;
            }

            pitchingMonthAverages.add((double) sum / count);
        }

        model.addAttribute("pitchingMonthLabels", pitchingMonthLabels);
        model.addAttribute("pitchingMonthAverages", pitchingMonthAverages);

        // ============================
        // ★ RISP 得点圏打率をhttps://baseball.yahoo.co.jp/mlb/player/2100825/rs
        // ここから取得（Yahoo方式）
        // ============================
        // Map<String, String> risp = mlbGameService.getRispFromYahoo();
        // System.out.println("RISP処理呼び出し開始");
        // model.addAttribute("rispAvg", risp.get("avg"));
        // model.addAttribute("rispDetail", risp.get("detail"));

        // ============================
        // ★ RISP 得点圏打率をohtani_game_details
        // ここから取得（自前DB）
        // ============================
        System.out.println("RISP処理呼び出し開始");
        Map<String, Object> risp = mlbGameService.getRispFromDB();
        model.addAttribute("rispAvg", risp.get("avg"));
        model.addAttribute("rispDetail", risp.get("detail"));

        System.out.println("対右投手処理呼び出し開始");
        // model.addAttribute("vsRAvg", mlbGameService.getVsRightAvgFormatted());
        Map<String, String> vsR = mlbGameService.getVsRightStatsFormatted();
        model.addAttribute("vsRAvg", vsR.get("avg"));
        model.addAttribute("vsRDetail", vsR.get("detail"));

        return "hogehoge_01";
    }

    /**
     * ============================
     * 投手シーズン俯瞰（hogehoge_03）
     * ============================
     */
    @GetMapping("/hogehoge_03")
    public String showPitchingOverview(Model model) {

        // ============================
        // ★ 直近 PITTIN’
        // ============================
        OhtaniPitchingGame latestPitchingGame = pitchingGameRepository.findTopByOrderByGameDateDesc();

        if (latestPitchingGame != null) {

            model.addAttribute(
                    "latestPitchingGameDate",
                    latestPitchingGame.getGameDate());

            String rank = latestPitchingGame.getFormValue(); // "S","A","B"…
            model.addAttribute("latestPitchingFormRank", rank);

            String emoji = switch (rank) {
                case "S" -> "🔥";
                case "A" -> "😊";
                case "B" -> "😳";
                case "C" -> "😣";
                default -> "🧊";
            };
            model.addAttribute("latestPitchingFormEmoji", emoji);

            model.addAttribute(
                    "latestPitchingComment",
                    latestPitchingGame.getComment());
        }

        // ============================
        // ★ 月平均グラフ（既存）
        // ============================
        int year = 2026;

        List<String> pitchingMonthLabels = new ArrayList<>();
        List<Double> pitchingMonthAverages = new ArrayList<>();

        for (int m = 3; m <= 11; m++) {
            LocalDate start = LocalDate.of(year, m, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            List<OhtaniPitchingGame> games = pitchingGameRepository.findByGameDateBetween(start, end);

            pitchingMonthLabels.add(m + "月");

            if (games.isEmpty()) {
                pitchingMonthAverages.add(null);
                continue;
            }

            int sum = 0;
            for (OhtaniPitchingGame g : games) {
                sum += switch (g.getFormValue()) {
                    case "S" -> 5;
                    case "A" -> 4;
                    case "B" -> 3;
                    case "C" -> 2;
                    default -> 1;
                };
            }
            pitchingMonthAverages.add((double) sum / games.size());
        }

        model.addAttribute("pitchingMonthLabels", pitchingMonthLabels);
        model.addAttribute("pitchingMonthAverages", pitchingMonthAverages);

        return "hogehoge_03";
    }

    /**
     * ============================
     * BATTING：旬平均
     * ============================
     */
    private Integer avgForm(int year, int month, int startDay, int endDay) {

        LocalDate start = LocalDate.of(year, month, startDay);
        LocalDate end = LocalDate.of(year, month, endDay);

        List<OhtaniGame> games = gameRepository.findGamesBetween(start, end);

        if (games.isEmpty()) {
            return null;
        }

        double avg = games.stream()
                .map(OhtaniGame::getFormValue)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(Double.NaN);

        if (Double.isNaN(avg)) {
            return null;
        }

        return (int) Math.round(avg);
    }

    private String convertFormRank(Integer value) {
        if (value == null)
            return "-";
        return switch (value) {
            case 2 -> "S";
            case 1 -> "A";
            case 0 -> "B";
            case -1 -> "C";
            case -2 -> "D";
            default -> "-";
        };
    }

    private String convertFormEmoji(Integer value) {
        if (value == null)
            return "";
        return switch (value) {
            case 2 -> "🔥";
            case 1 -> "😊";
            case 0 -> "😳";
            case -1 -> "😣";
            case -2 -> "🧊";
            default -> "";
        };
    }

    /**
     * ============================================
     * ★ バッティングフィルタ（投手タイプ選択）
     * ============================================
     */
    @GetMapping("/batting/filter")
    public String showBattingFilter(
            @RequestParam(required = false) String hand,
            @RequestParam(required = false) String result, // ★追加
            @RequestParam(required = false) String opponent, // ★追加
            Model model) {

        // 🔥 ALL（明示的に選んだ時だけ）
        if ("ALL".equals(hand)) {

            Map<String, String> vsAll = mlbGameService.getVsAllStatsFormatted();

            model.addAttribute("vsAllAvg", vsAll.get("avg"));
            model.addAttribute("vsAllDetail", vsAll.get("detail"));
            model.addAttribute("vsAllLogs", mlbGameService.getVsAllLogs(result, opponent));
        }

        // 右
        if ("R".equals(hand)) {

            Map<String, String> vsR = mlbGameService.getVsRightStatsFormatted();

            model.addAttribute("vsRAvg", vsR.get("avg"));
            model.addAttribute("vsRDetail", vsR.get("detail"));
            model.addAttribute("vsRLogs", mlbGameService.getVsRightLogs(result, opponent));
        }

        // 左
        if ("L".equals(hand)) {

            Map<String, String> vsL = mlbGameService.getVsLeftStatsFormatted();

            model.addAttribute("vsLAvg", vsL.get("avg"));
            model.addAttribute("vsLDetail", vsL.get("detail"));
            model.addAttribute("vsLLogs", mlbGameService.getVsLeftLogs(result, opponent));
        }

        // ★ チーム一覧を取得
        List<String> opponents = mlbGameService.getAllOpponents();

        // ★ 画面へ渡す
        model.addAttribute("opponents", opponents);

        return "batting_filter";
    }
}
