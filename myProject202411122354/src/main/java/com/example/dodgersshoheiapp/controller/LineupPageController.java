package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.dto.LineupResponse;
import com.example.dodgersshoheiapp.service.MlbLineupService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.*;

/**
 * ページ表示用。Serviceを直接呼ぶ。
 */
@Controller
public class LineupPageController {

    private static final int DODGERS_ID = 119; // ドジャース固定
    private static final ZoneId JST = ZoneId.of("Asia/Tokyo"); // 日本時間
    private static final LocalTime CUTOFF = LocalTime.of(18, 0);// 18:00を境に翌日へ

    private final MlbLineupService lineupService;

    public LineupPageController(MlbLineupService lineupService) {
        this.lineupService = lineupService;
    }

    /** 既存：/games/{gamePk}/lineups を直接表示 */
    @GetMapping("/games/{gamePk}/lineups")
    public String showLineups(@PathVariable long gamePk, Model model) {
        LineupResponse res = lineupService.fetchLineups(gamePk);
        model.addAttribute("home", res.home());
        model.addAttribute("away", res.away());
        return "lineups"; // templates/lineups.html
    }

    /**
     * JST 18:00 までは「当日」、18:00 を過ぎたら「翌日」のドジャース戦へ自動遷移。
     * その日に試合が無ければ、先の試合（最短の非Final）へフォールバック。
     */
    @GetMapping("/dodgers/lineups/auto")
    public String showDodgersSmart() {
        LocalDate target = decideTargetDateWithCutoff(JST, CUTOFF);
        var pkOpt = lineupService.findGamePkByDate(DODGERS_ID, target);

        if (pkOpt.isEmpty()) {
            // 当日に試合が無い場合は、最大7日先までで最初の非Finalを拾う
            pkOpt = lineupService.findNextGamePk(DODGERS_ID, target, 7);
        }

        if (pkOpt.isEmpty()) {
            return "redirect:/games/no-game"; // 必要に応じて専用ページへ
        }
        return "redirect:/games/" + pkOpt.getAsLong() + "/lineups";
    }

    /** 任意日付指定（?date=YYYY-MM-DD）。無指定なら「auto」ロジックと同じ挙動に。 */
    @GetMapping("/dodgers/lineups")
    public String showDodgersByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate target = (date != null) ? date : decideTargetDateWithCutoff(JST, CUTOFF);
        var pkOpt = lineupService.findGamePkByDate(DODGERS_ID, target);
        if (pkOpt.isEmpty()) {
            pkOpt = lineupService.findNextGamePk(DODGERS_ID, target, 7);
        }
        return pkOpt.isEmpty()
                ? "redirect:/games/no-game"
                : "redirect:/games/" + pkOpt.getAsLong() + "/lineups";
    }

    /** JST18:00を境に、当日/翌日を決めるヘルパー */
private LocalDate decideTargetDateWithCutoff(ZoneId zone, LocalTime cutoff) {
    ZonedDateTime now = ZonedDateTime.now(zone);
    return now.toLocalDate(); // 18:00切替をやめ、常に当日
}
}