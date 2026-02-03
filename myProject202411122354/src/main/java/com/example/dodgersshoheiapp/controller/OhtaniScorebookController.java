package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.OhtaniGame;
import com.example.dodgersshoheiapp.repository.OhtaniGameRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // ★ 追加

@Controller
public class OhtaniScorebookController {

    private final OhtaniGameRepository gameRepository;

    public OhtaniScorebookController(OhtaniGameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    /**
     * シーズン俯瞰（hogehoge_01）
     */
    @GetMapping("/hogehoge_01")
    public String showSeasonOverview(Model model) {

        // ★ DBから最新試合日を取得
        LocalDate latestGameDate = gameRepository.findLatestGameDate();

        int latestMonth = latestGameDate.getMonthValue();
        int latestDay = latestGameDate.getDayOfMonth();

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        // ===== 4月 =====
        if (latestMonth >= 4) {
            labels.add("4月上旬");
            values.add(avgForm(2026, 4, 1, 10));

            labels.add("4月中旬");
            values.add(avgForm(2026, 4, 11, 20));

            labels.add("4月下旬");
            values.add(avgForm(2026, 4, 21, 30));
        }

        // ===== 5月 =====
        if (latestMonth >= 5) {
            labels.add("5月上旬");
            values.add(avgForm(2026, 5, 1, 10));

            labels.add("5月中旬");
            values.add(avgForm(2026, 5, 11, 20));

            labels.add("5月下旬");
            values.add(avgForm(2026, 5, 21, 31));
        }

        // ===== 6月 =====
        if (latestMonth >= 6) {
            labels.add("6月上旬");
            values.add(avgForm(2026, 6, 1, 10));

            labels.add("6月中旬");
            values.add(avgForm(2026, 6, 11, 20));

            labels.add("6月下旬");
            values.add(avgForm(2026, 6, 21, 30));
        }

        // ===== 7月 =====
        if (latestMonth >= 7) {
            labels.add("7月上旬");
            values.add(avgForm(2026, 7, 1, 10));

            labels.add("7月中旬");
            values.add(avgForm(2026, 7, 11, 20));

            labels.add("7月下旬");
            values.add(avgForm(2026, 7, 21, 31));
        }

        // ===== 8月 =====
        if (latestMonth >= 8) {
            labels.add("8月上旬");
            values.add(avgForm(2026, 8, 1, 10));

            labels.add("8月中旬");
            values.add(avgForm(2026, 8, 11, 20));

            labels.add("8月下旬");
            values.add(avgForm(2026, 8, 21, 31));
        }

        // ===== 9月 =====
        if (latestMonth >= 9) {
            labels.add("9月上旬");
            values.add(avgForm(2026, 9, 1, 10));

            labels.add("9月中旬");
            values.add(avgForm(2026, 9, 11, 20));

            labels.add("9月下旬");
            values.add(avgForm(2026, 9, 21, 30));
        }

        // ===== 10月 =====
        if (latestMonth >= 10) {
            labels.add("10月上旬");
            values.add(avgForm(2026, 10, 1, 10));

            labels.add("10月中旬");
            values.add(avgForm(2026, 10, 11, 20));

            labels.add("10月下旬");
            values.add(avgForm(2026, 10, 21, 31));
        }

        // ===== 11月 =====
        if (latestMonth >= 11) {
            labels.add("11月上旬");
            values.add(avgForm(2026, 11, 1, 10));

            labels.add("11月中旬");
            values.add(avgForm(2026, 11, 11, 20));

            labels.add("11月下旬");
            values.add(avgForm(2026, 11, 21, 30));
        }

        model.addAttribute("labels", labels);
        model.addAttribute("values", values);

        return "hogehoge_01";
    }

    /**
     * 旬ごとの評価平均（form_value）
     */
    // OhtaniScorebookController.java
    // avgForm() メソッド（★ここだけ修正）

    private Integer avgForm(int year, int month, int startDay, int endDay) {

        LocalDate start = LocalDate.of(year, month, startDay);
        LocalDate end = LocalDate.of(year, month, endDay);

        List<OhtaniGame> games = gameRepository.findGamesBetween(start, end);

        // ★ データが無ければ「点を出さない」
        if (games.isEmpty()) {
            return null;
        }

        // ★ DB (-2〜2) をそのまま平均する
        double avg = games.stream()
                .map(OhtaniGame::getFormValue)
                .filter(Objects::nonNull) // ★ 存在するデータのみ
                .mapToInt(Integer::intValue)
                .average()
                .orElse(Double.NaN);

        if (Double.isNaN(avg)) {
            return null;
        }

        // ★ 四捨五入（切り捨て禁止）
        return (int) Math.round(avg);
    }
}
