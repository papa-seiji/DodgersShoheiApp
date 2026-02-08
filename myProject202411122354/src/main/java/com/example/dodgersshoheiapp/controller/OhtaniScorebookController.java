package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.OhtaniGame;
import com.example.dodgersshoheiapp.model.OhtaniPitchingGame;
import com.example.dodgersshoheiapp.repository.OhtaniGameRepository;
import com.example.dodgersshoheiapp.repository.OhtaniPitchingGameRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
public class OhtaniScorebookController {

    private final OhtaniGameRepository gameRepository;
    private final OhtaniPitchingGameRepository pitchingGameRepository;

    public OhtaniScorebookController(
            OhtaniGameRepository gameRepository,
            OhtaniPitchingGameRepository pitchingGameRepository) {
        this.gameRepository = gameRepository;
        this.pitchingGameRepository = pitchingGameRepository;
    }

    /**
     * ============================
     * シーズン俯瞰（hogehoge_01）
     * ============================
     */
    @GetMapping("/hogehoge_01")
    public String showSeasonOverview(Model model) {

        // ===== 既存（BATTING） =====
        LocalDate latestGameDate = gameRepository.findLatestGameDate();
        OhtaniGame latestGame = gameRepository.findLatestGame();

        model.addAttribute("latestGameDate", latestGame.getGameDate());

        String latestFormRank = convertFormRank(latestGame.getFormValue());
        model.addAttribute("latestFormRank", latestFormRank);

        String latestFormEmoji = convertFormEmoji(latestGame.getFormValue());
        model.addAttribute("latestFormEmoji", latestFormEmoji);

        model.addAttribute("latestComment", latestGame.getComment());

        int latestMonth = latestGameDate.getMonthValue();

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        // ===== BATTING（旬平均）※既存ロジック踏襲 =====
        for (int m = 4; m <= Math.min(latestMonth, 11); m++) {
            labels.add(m + "月上旬");
            values.add(avgForm(2026, m, 1, 10));

            labels.add(m + "月中旬");
            values.add(avgForm(2026, m, 11, 20));

            labels.add(m + "月下旬");
            values.add(avgForm(
                    2026,
                    m,
                    21,
                    LocalDate.of(2026, m, 1).lengthOfMonth()));
        }

        model.addAttribute("labels", labels);
        model.addAttribute("values", values);

        // ============================
        // ★ 追加：PITCHING（月平均・4〜11月）
        // ============================
        List<String> pitchingMonthLabels = new ArrayList<>();
        List<Double> pitchingMonthAverages = new ArrayList<>();

        int year = 2026;

        for (int m = 4; m <= 11; m++) {

            LocalDate start = LocalDate.of(year, m, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            List<OhtaniPitchingGame> games = pitchingGameRepository.findByGameDateBetween(start, end);

            pitchingMonthLabels.add(m + "月");

            if (games.isEmpty()) {
                // 登板なし → 点を出さない
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
                    default -> 1; // D or 想定外
                };
                sum += value;
                count++;
            }

            pitchingMonthAverages.add((double) sum / count);
        }

        model.addAttribute("pitchingMonthLabels", pitchingMonthLabels);
        model.addAttribute("pitchingMonthAverages", pitchingMonthAverages);

        return "hogehoge_01";
    }

    /**
     * ============================
     * 投手シーズン俯瞰（hogehoge_03）
     * ============================
     */
    @GetMapping("/hogehoge_03")
    public String showPitchingOverview(Model model) {

        int year = 2026;

        List<String> pitchingMonthLabels = new ArrayList<>();
        List<Double> pitchingMonthAverages = new ArrayList<>();

        for (int m = 4; m <= 11; m++) {

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
                    default -> 1; // D
                };
                sum += value;
                count++;
            }

            pitchingMonthAverages.add((double) sum / count);
        }

        model.addAttribute("pitchingMonthLabels", pitchingMonthLabels);
        model.addAttribute("pitchingMonthAverages", pitchingMonthAverages);

        return "hogehoge_03";
    }

    /**
     * ============================
     * 旬ごとの評価平均（BATTING 用）
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

    /**
     * form_value（-2〜2） → ランク（S〜D）
     */
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

    /**
     * form_value（-2〜2） → 絵文字
     */
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
}
