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

    /** 既存：/games/{gamePk}/lineups を直接表示（表示日は gameInfo の JST を使う） */
    @GetMapping("/games/{gamePk}/lineups")
    public String showLineups(@PathVariable long gamePk, Model model) {
        LineupResponse res = lineupService.fetchLineups(gamePk);
        model.addAttribute("home", res.home());
        model.addAttribute("away", res.away());
        model.addAttribute("gameInfo", res.gameInfo()); // JST 変換済み
        return "lineups";
    }

    /** 自動切替（JST18:00までは当日、以降は翌日）。無ければ次戦へフォールバック */
    @GetMapping("/dodgers/lineups/auto")
    public String showDodgersSmart() {
        LocalDate target = decideTargetDateWithCutoff(JST, CUTOFF);
        var pkOpt = lineupService.findGamePkByDate(DODGERS_ID, target);
        if (pkOpt.isEmpty()) {
            pkOpt = lineupService.findNextGamePk(DODGERS_ID, target, 7);
        }
        return pkOpt.isEmpty()
                ? "redirect:/games/no-game"
                : "redirect:/games/" + pkOpt.getAsLong() + "/lineups";
    }

    /** 日付指定（?date=YYYY-MM-DD）。未指定なら auto と同じ挙動で gamePk を解決 */
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

    /** （取得用）JST18:00を境に当日/翌日を返すヘルパー。表示日には使わない */
    private LocalDate decideTargetDateWithCutoff(ZoneId zone, LocalTime cutoff) {
        ZonedDateTime now = ZonedDateTime.now(zone);
        return now.toLocalTime().isBefore(cutoff) ? now.toLocalDate() : now.toLocalDate().plusDays(1);
    }
}
