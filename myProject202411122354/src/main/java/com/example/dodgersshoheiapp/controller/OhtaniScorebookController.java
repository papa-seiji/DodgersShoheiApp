package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.dto.OpsTrendDto;
import com.example.dodgersshoheiapp.model.OhtaniGame;
import com.example.dodgersshoheiapp.model.OhtaniPitchingGame;
import com.example.dodgersshoheiapp.repository.OhtaniGameRepository;
import com.example.dodgersshoheiapp.repository.OhtaniPitchingGameRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.example.dodgersshoheiapp.dto.OpsTrendDto;
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
                Map<String, String> vsR = mlbGameService.getVsRightStatsFormatted(2026);
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
                        @RequestParam(required = false) Integer season,

                        @RequestParam(required = false) String hand,
                        @RequestParam(required = false) String result, // ★追加
                        @RequestParam(required = false) String opponent, // ★追加
                        @RequestParam(required = false) String pitcher, // ★追加
                        @RequestParam(required = false) String pitchType, // ★追加
                        @RequestParam(required = false) Integer speedMin, // ★追加
                        @RequestParam(required = false) Integer speedMax, // ★追加

                        @RequestParam(required = false) String pitcherHand, // RISP系 ★追加
                        Model model) {
                int currentYear = LocalDate.now().getYear();

                if (season == null) {
                        season = currentYear;
                }

                // 🔥 ALL（明示的に選んだ時だけ）
                if ("ALL".equals(hand)) {

                        Map<String, String> vsAll;

                        // ============================================
                        // ★ pitcher 指定時（最優先）
                        // ============================================
                        if (pitcher != null
                                        && !pitcher.isBlank()) {

                                vsAll = mlbGameService
                                                .getVsAllStatsByPitcherFormatted(pitcher);

                                // ============================================
                                // ★ 球速帯分析モード
                                // ============================================
                        } else if (speedMin != null
                                        && speedMax != null
                                        && (pitchType == null
                                                        || pitchType.isBlank()
                                                        || "ALL".equals(pitchType))
                                        && (opponent == null
                                                        || opponent.isBlank()
                                                        || "ALL".equals(opponent))) {

                                vsAll = mlbGameService
                                                .getVsAllStatsBySpeedFormatted(
                                                                speedMin,
                                                                speedMax,
                                                                season);

                                // ============================================
                                // ★ pitchType 指定時
                                // ============================================
                        } else if (pitchType != null
                                        && !pitchType.isBlank()
                                        && !"ALL".equals(pitchType)) {

                                vsAll = mlbGameService
                                                .getVsAllStatsByPitchTypeFormatted(pitchType, season);

                                // ============================================
                                // ★ opponent 指定時
                                // ============================================
                        } else if (opponent != null
                                        && !opponent.isBlank()
                                        && !"ALL".equals(opponent)) {

                                vsAll = mlbGameService
                                                .getVsAllStatsByOpponentFormatted(opponent, season);

                        } else {

                                vsAll = mlbGameService
                                                .getVsAllStatsFormatted(opponent, season);
                        }

                        model.addAttribute("vsAllAvg", vsAll.get("avg"));
                        model.addAttribute("vsAllDetail", vsAll.get("detail"));

                        model.addAttribute("vsAllAvg", vsAll.get("avg"));
                        model.addAttribute("vsAllDetail", vsAll.get("detail"));
                        // ★ここ修正
                        model.addAttribute("vsAllLogs",
                                        mlbGameService.getVsAllLogs(
                                                        result,
                                                        opponent,
                                                        pitcher,
                                                        pitchType,
                                                        speedMin,
                                                        speedMax,
                                                        season));
                }

                // ============================================
                // ★ opponent × pitcher 整合性チェック
                // ============================================
                if (opponent != null
                                && !opponent.isBlank()
                                && pitcher != null
                                && !pitcher.isBlank()) {

                        boolean exists = mlbGameService.existsPitcherInOpponent(
                                        pitcher,
                                        opponent);

                        if (!exists) {

                                String actualOpponent = mlbGameService.getOpponentByPitcher(
                                                pitcher);

                                model.addAttribute(
                                                "pitcherOpponentWarning",
                                                "⚠ "
                                                                + pitcher
                                                                + " は "
                                                                + actualOpponent
                                                                + " の投手です。チーム選択を確認してください");
                        }
                }

                // ============================================
                // ★ RISP（ALL）---------------------------------batting/filter用
                // ============================================
                Map<String, Object> rispAll = mlbGameService.getRispFromDBByHand(
                                null,
                                season);

                model.addAttribute(
                                "rispAllAvg",
                                rispAll.get("avg"));

                model.addAttribute(
                                "rispAllDetail",
                                rispAll.get("detail"));

                // ============================================
                // ★ RISPログ ---------------------------------batting/filter用
                // ============================================
                model.addAttribute(
                                "rispLogs",
                                mlbGameService.getRispLogs(
                                                pitcherHand,
                                                season));

                // ============================================
                // ★ 右
                // ============================================

                if ("R".equals(hand)) {

                        Map<String, String> vsR;

                        // ============================================
                        // ★ pitcher 指定時（最優先）
                        // ============================================
                        if (pitcher != null
                                        && !pitcher.isBlank()) {

                                vsR = mlbGameService
                                                .getVsRightStatsByPitcherFormatted(pitcher);

                                // ============================================
                                // ★ 球速帯分析モード
                                // 条件：
                                // ・speed指定あり
                                // ・pitchType=ALL
                                // ・opponent=ALL
                                // ============================================
                        } else if (speedMin != null
                                        && speedMax != null
                                        && (pitchType == null
                                                        || pitchType.isBlank()
                                                        || "ALL".equals(pitchType))
                                        && (opponent == null
                                                        || opponent.isBlank()
                                                        || "ALL".equals(opponent))) {

                                vsR = mlbGameService
                                                .getVsRightStatsBySpeedFormatted(
                                                                speedMin,
                                                                speedMax,
                                                                season);

                                // ============================================
                                // ★ pitchType 指定時
                                // ============================================
                        } else if (pitchType != null
                                        && !pitchType.isBlank()
                                        && !"ALL".equals(pitchType)) {

                                vsR = mlbGameService
                                                .getVsRightStatsByPitchTypeFormatted(pitchType, season);

                                // ============================================
                                // ★ opponent 指定時
                                // ============================================
                        } else if (opponent != null
                                        && !opponent.isBlank()
                                        && !"ALL".equals(opponent)) {

                                vsR = mlbGameService
                                                .getVsRightStatsByOpponentFormatted(
                                                                opponent,
                                                                season);

                        } else {

                                vsR = mlbGameService
                                                .getVsRightStatsFormatted(season);
                        }

                        model.addAttribute("vsRAvg", vsR.get("avg"));
                        model.addAttribute("vsRDetail", vsR.get("detail"));

                        model.addAttribute("vsRAvg", vsR.get("avg"));
                        model.addAttribute("vsRDetail", vsR.get("detail"));
                        // ★ここ修正
                        model.addAttribute("vsRLogs",
                                        mlbGameService.getVsRightLogs(
                                                        result,
                                                        opponent,
                                                        pitcher,
                                                        pitchType,
                                                        speedMin,
                                                        speedMax,
                                                        season));
                }

                // ============================================
                // ★ 左
                // ============================================

                if ("L".equals(hand)) {

                        Map<String, String> vsL;

                        // ============================================
                        // ★ pitcher 指定時（最優先）
                        // ============================================
                        if (pitcher != null
                                        && !pitcher.isBlank()) {

                                vsL = mlbGameService
                                                .getVsLeftStatsByPitcherFormatted(pitcher);

                                // ============================================
                                // ★ 球速帯分析モード
                                // ============================================
                        } else if (speedMin != null
                                        && speedMax != null
                                        && (pitchType == null
                                                        || pitchType.isBlank()
                                                        || "ALL".equals(pitchType))
                                        && (opponent == null
                                                        || opponent.isBlank()
                                                        || "ALL".equals(opponent))) {

                                vsL = mlbGameService
                                                .getVsLeftStatsBySpeedFormatted(
                                                                speedMin,
                                                                speedMax,
                                                                season);

                                // ============================================
                                // ★ pitchType 指定時
                                // ============================================
                        } else if (pitchType != null
                                        && !pitchType.isBlank()
                                        && !"ALL".equals(pitchType)) {

                                vsL = mlbGameService
                                                .getVsLeftStatsByPitchTypeFormatted(pitchType, season);

                                // ============================================
                                // ★ opponent 指定時
                                // ============================================
                        } else if (opponent != null
                                        && !opponent.isBlank()
                                        && !"ALL".equals(opponent)) {

                                vsL = mlbGameService
                                                .getVsLeftStatsByOpponentFormatted(
                                                                opponent,
                                                                season);

                        } else {

                                vsL = mlbGameService
                                                .getVsLeftStatsFormatted(season);
                        }

                        model.addAttribute("vsLAvg", vsL.get("avg"));
                        model.addAttribute("vsLDetail", vsL.get("detail"));

                        model.addAttribute("vsLAvg", vsL.get("avg"));
                        model.addAttribute("vsLDetail", vsL.get("detail"));
                        // ★ここ修正
                        model.addAttribute("vsLLogs",
                                        mlbGameService.getVsLeftLogs(
                                                        result,
                                                        opponent,
                                                        pitcher,
                                                        pitchType,
                                                        speedMin,
                                                        speedMax,
                                                        season));
                }

                // ============================================
                // ★ 検索条件サマリー
                // ★ 何フィルタで検索しているか表示（パンくず的表示）
                // ============================================

                String handLabel = ("R".equals(hand)) ? "右"
                                : ("L".equals(hand)) ? "左"
                                                : ("ALL".equals(hand)) ? "ALL"
                                                                : "未選択";

                String resultLabel = (result == null || result.isBlank())
                                ? "ALL"
                                : result;

                String opponentLabel = (opponent == null || opponent.isBlank())
                                ? "なし"
                                : opponent;

                String pitcherLabel = (pitcher == null || pitcher.isBlank())
                                ? "なし"
                                : pitcher;

                String pitchTypeLabel = (pitchType == null
                                || pitchType.isBlank()
                                || "ALL".equals(pitchType))
                                                ? "なし"
                                                : pitchType;

                String speedLabel = (speedMin != null && speedMax != null)
                                ? speedMin + "～" + speedMax + "mph"
                                : "なし";

                String searchSummaryHtml =

                                "<div class='summary-badge'>"
                                                + "<span class='badge-key'>投手タイプ</span>"
                                                + "<span class='badge-value'>対" + handLabel + "</span>"
                                                + "</div>"

                                                + "<div class='summary-badge'>"
                                                + "<span class='badge-key'>打席結果</span>"
                                                + "<span class='badge-value'>" + resultLabel + "</span>"
                                                + "</div>"

                                                + "<div class='summary-badge'>"
                                                + "<span class='badge-key'>チーム</span>"
                                                + "<span class='badge-value'>" + opponentLabel + "</span>"
                                                + "</div>"

                                                + "<div class='summary-badge'>"
                                                + "<span class='badge-key'>投手名</span>"
                                                + "<span class='badge-value'>" + pitcherLabel + "</span>"
                                                + "</div>"

                                                + "<div class='summary-badge'>"
                                                + "<span class='badge-key'>球種</span>"
                                                + "<span class='badge-value'>" + pitchTypeLabel + "</span>"
                                                + "</div>"

                                                + "<div class='summary-badge'>"
                                                + "<span class='badge-key'>球速</span>"
                                                + "<span class='badge-value'>" + speedLabel + "</span>"
                                                + "</div>";

                model.addAttribute(
                                "searchSummaryHtml",
                                searchSummaryHtml);

                // ★ チーム一覧を取得
                List<String> opponents = mlbGameService.getAllOpponents();

                /*
                 * ============================================
                 * ★ 投手左右チェック（左右が違えば警告を出して再入力を促す）
                 * ============================================
                 */
                if (pitcher != null && !pitcher.isBlank()
                                && hand != null && !hand.isBlank()
                                && !"ALL".equals(hand)) {

                        String actualPitcherHand = mlbGameService.getPitcherHand(pitcher);

                        // ★ R選択なのに左投手
                        if ("R".equals(hand)
                                        && "L".equals(actualPitcherHand)) {

                                model.addAttribute(
                                                "pitcherWarning",
                                                pitcher + " は左投手です");

                        }

                        // ★ L選択なのに右投手
                        if ("L".equals(hand)
                                        && "R".equals(actualPitcherHand)) {

                                model.addAttribute(
                                                "pitcherWarning",
                                                pitcher + " は右投手です");
                        }
                }

                // ============================================
                // ★ 累積OPS日次推移グラフ、vsALL-第三弾 投手タイプフィルタと連動して[ vs ALL / R / L] 表示
                // ============================================
                String opsHand = (hand == null || hand.isBlank())
                                ? "ALL"
                                : hand;

                // ============================================
                // ★ 累積OPS日次推移グラフ、どれが選択されて表示されているかを再度ここでもバッジ形式で中に表示
                // ============================================
                String opsHandBadge;

                switch (opsHand) {

                        case "R":
                                opsHandBadge = "VS 右ピッチャー";
                                break;

                        case "L":
                                opsHandBadge = "VS 左ピッチャー";
                                break;

                        default:
                                opsHandBadge = "VS ALLピッチャー";
                }

                // ============================================
                // ★ 累積OPS推移
                // ============================================
                List<OpsTrendDto> opsTrend = gameRepository.getCumulativeOpsTrend(opsHand, season);

                List<OpsTrendDto> allOpsTrend = gameRepository.getCumulativeOpsTrend("ALL", season);

                List<OpsTrendDto> rOpsTrend = gameRepository.getCumulativeOpsTrend("R", season);

                List<OpsTrendDto> lOpsTrend = gameRepository.getCumulativeOpsTrend("L", season);

                List<String> opsLabels = new ArrayList<>();
                List<Double> opsValues = new ArrayList<>();

                for (OpsTrendDto dto : opsTrend) {

                        opsLabels.add(dto.getGameDate());
                        opsValues.add(dto.getCumulativeOps());
                }

                model.addAttribute("opsLabels", opsLabels);
                model.addAttribute("opsValues", opsValues);
                model.addAttribute("opsHandBadge", opsHandBadge);
                model.addAttribute(
                                "allOpsValues",
                                allOpsTrend.stream()
                                                .map(OpsTrendDto::getCumulativeOps)
                                                .toList());

                model.addAttribute(
                                "rOpsValues",
                                rOpsTrend.stream()
                                                .map(OpsTrendDto::getCumulativeOps)
                                                .toList());

                model.addAttribute(
                                "lOpsValues",
                                lOpsTrend.stream()
                                                .map(OpsTrendDto::getCumulativeOps)
                                                .toList());

                // ★ 画面へ渡す
                model.addAttribute("opponents", opponents);

                model.addAttribute("season", season);
                model.addAttribute("currentYear", currentYear);

                return "batting_filter";
        }

        // ============================================
        // ★ 投手 → opponent 自動補完 API
        // ============================================
        @GetMapping("/api/pitcher-opponent")
        @ResponseBody
        public String getPitcherOpponent(
                        @RequestParam String pitcher) {

                return mlbGameService
                                .getOpponentByPitcher(pitcher);
        }

        /**
         * ============================================
         * ★ 投手サジェストAPI
         * ============================================
         */
        @GetMapping("/api/pitchers")
        @ResponseBody
        public List<String> getPitchers(

                        @RequestParam String keyword,

                        @RequestParam(required = false) String opponent) {

                return mlbGameService
                                .searchPitchers(
                                                keyword,
                                                opponent);
        }
}
