package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.dto.LineupResponse;
import com.example.dodgersshoheiapp.service.MlbLineupService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Controller
public class LineupPageController {

    private static final int DODGERS_ID = 119;
    private static final ZoneId JST = ZoneId.of("Asia/Tokyo");
    private static final LocalTime CUTOFF = LocalTime.of(18, 0);

    private final MlbLineupService lineupService;

    public LineupPageController(MlbLineupService lineupService) {
        this.lineupService = lineupService;
    }

    /** フルページ：gamePk 固定表示（デフォルトは左=AWAY, 右=HOME） */
    @GetMapping("/games/{gamePk}/lineups")
    public String showLineups(
            @PathVariable long gamePk,
            @RequestParam(name = "left", defaultValue = "away") String left,
            Model model) {

        LineupResponse res = lineupService.fetchLineups(gamePk);

        boolean leftIsAway = "away".equalsIgnoreCase(left);
        model.addAttribute("left", leftIsAway ? res.away() : res.home());
        model.addAttribute("right", leftIsAway ? res.home() : res.away());
        model.addAttribute("gameInfo", res.gameInfo());
        return "lineups";
    }

    /**
     * Auto（フルページ向け）:
     * 1) 基準日(base=JSTカットオフ適用)に試合が無ければ「次の試合」を最優先で表示
     * 2) 試合がある日は従来どおり offsetDays を反映し、その日が見つからなければ
     * まず「次」を探し、無ければ「前」を探す
     */
    @GetMapping("/dodgers/lineups/auto")
    public String showDodgersSmart(
            @RequestParam(name = "offset", defaultValue = "-1") int offsetDays) {

        LocalDate base = decideTargetDateWithCutoff(JST, CUTOFF);

        // まずは base 当日の試合有無をチェック
        var todayOpt = lineupService.findGamePkByDate(DODGERS_ID, base);

        java.util.OptionalLong pkOpt;
        if (todayOpt.isEmpty()) {
            // 休養日など → 次の試合を最優先
            pkOpt = lineupService.findNextGamePk(DODGERS_ID, base, 14);
            if (pkOpt.isEmpty()) {
                pkOpt = lineupService.findPrevGamePk(DODGERS_ID, base, 14);
            }
        } else {
            // 試合日 → 従来の offset ロジックで探し、無ければ次→前の順でフォールバック
            LocalDate target = base.plusDays(offsetDays);
            pkOpt = lineupService.findGamePkByDate(DODGERS_ID, target);
            if (pkOpt.isEmpty()) {
                pkOpt = lineupService.findNextGamePk(DODGERS_ID, target, 14);
                if (pkOpt.isEmpty()) {
                    pkOpt = lineupService.findPrevGamePk(DODGERS_ID, target, 14);
                }
            }
        }

        return pkOpt.isEmpty()
                ? "redirect:/games/no-game"
                : "redirect:/games/" + pkOpt.getAsLong() + "/lineups?left=away";
    }

    /**
     * Manual date（フルページ向け）:
     * 指定日+offset をまず探し、無ければ「次→前」の順でフォールバック。
     * （date 未指定の場合は base=JST カットオフ日からの offset とし、
     * 最終的なフォールバックは同じく次→前）
     */
    @GetMapping("/dodgers/lineups")
    public String showDodgersByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "offset", defaultValue = "-1") int offsetDays) {

        LocalDate base = (date != null) ? date : decideTargetDateWithCutoff(JST, CUTOFF);
        LocalDate target = base.plusDays(offsetDays);

        var pkOpt = lineupService.findGamePkByDate(DODGERS_ID, target);
        if (pkOpt.isEmpty()) {
            // まず「次」を優先、無ければ「前」
            pkOpt = lineupService.findNextGamePk(DODGERS_ID, target, 14);
            if (pkOpt.isEmpty()) {
                pkOpt = lineupService.findPrevGamePk(DODGERS_ID, target, 14);
            }
        }

        return pkOpt.isEmpty()
                ? "redirect:/games/no-game"
                : "redirect:/games/" + pkOpt.getAsLong() + "/lineups?left=away";
    }

    /** JST 18:00 カットオフ：18時前は当日、以降は翌日を基準日とする */
    private LocalDate decideTargetDateWithCutoff(ZoneId zone, LocalTime cutoff) {
        ZonedDateTime now = ZonedDateTime.now(zone);
        return now.toLocalTime().isBefore(cutoff)
                ? now.toLocalDate()
                : now.toLocalDate().plusDays(1);
    }

    // Home 埋め込み用（小型フラグメント）：オフ日は「次の試合」を優先表示
    @GetMapping("/widgets/dodgers/lineups")
    public String lineupWidget(
            @RequestParam(name = "offset", defaultValue = "-1") int offsetDays,
            @RequestParam(name = "left", defaultValue = "away") String left,
            Model model) {

        // JST基準日（18:00切替）
        LocalDate base = decideTargetDateWithCutoff(JST, CUTOFF);

        // まず「今日(base)に試合があるか」を確認
        var todayOpt = lineupService.findGamePkByDate(DODGERS_ID, base);

        java.util.OptionalLong pkOpt;
        if (todayOpt.isEmpty()) {
            // 休養日など：**次の試合**を最優先で表示（見つからなければ前へ）
            pkOpt = lineupService.findNextGamePk(DODGERS_ID, base, 14);
            if (pkOpt.isEmpty()) {
                pkOpt = lineupService.findPrevGamePk(DODGERS_ID, base, 14);
            }
        } else {
            // 試合がある日：従来どおり offset を反映
            LocalDate target = base.plusDays(offsetDays);
            pkOpt = lineupService.findGamePkByDate(DODGERS_ID, target);
            if (pkOpt.isEmpty()) {
                // 念のためフォールバックは **次→前** の順
                pkOpt = lineupService.findNextGamePk(DODGERS_ID, target, 14);
                if (pkOpt.isEmpty()) {
                    pkOpt = lineupService.findPrevGamePk(DODGERS_ID, target, 14);
                }
            }
        }

        if (pkOpt.isEmpty()) {
            model.addAttribute("left", null);
            model.addAttribute("right", null);
            model.addAttribute("gameInfo", null);
            return "fragments/lineups_widget :: widget";
        }

        var res = lineupService.fetchLineups(pkOpt.getAsLong());

        boolean leftIsAway = "away".equalsIgnoreCase(left);
        model.addAttribute("left", leftIsAway ? res.away() : res.home());
        model.addAttribute("right", leftIsAway ? res.home() : res.away());
        model.addAttribute("gameInfo", res.gameInfo());
        return "fragments/lineups_widget :: widget";
    }
}
