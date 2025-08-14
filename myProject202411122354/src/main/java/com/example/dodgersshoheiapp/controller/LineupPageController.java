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

    /** Show page for a fixed gamePk */
    @GetMapping("/games/{gamePk}/lineups")
    public String showLineups(@PathVariable long gamePk, Model model) {
        LineupResponse res = lineupService.fetchLineups(gamePk);
        model.addAttribute("home", res.home());
        model.addAttribute("away", res.away());
        model.addAttribute("gameInfo", res.gameInfo());
        return "lineups";
    }

    /**
     * Auto: base date by JST cutoff, then apply offset days (e.g. offset=-1 ->
     * previous day)
     */
    @GetMapping("/dodgers/lineups/auto")
    public String showDodgersSmart(
            @RequestParam(name = "offset", defaultValue = "-1") int offsetDays) {

        LocalDate base = decideTargetDateWithCutoff(JST, CUTOFF);
        LocalDate target = base.plusDays(offsetDays);

        var pkOpt = lineupService.findGamePkByDate(DODGERS_ID, target);
        if (pkOpt.isEmpty()) {
            // fallback depending on direction
            if (offsetDays < 0) {
                pkOpt = lineupService.findPrevGamePk(DODGERS_ID, target, 10);
            } else {
                pkOpt = lineupService.findNextGamePk(DODGERS_ID, target, 10);
            }
        }
        return pkOpt.isEmpty()
                ? "redirect:/games/no-game"
                : "redirect:/games/" + pkOpt.getAsLong() + "/lineups";
    }

    /** Manual date: ?date=YYYY-MM-DD plus optional offset=? */
    @GetMapping("/dodgers/lineups")
    public String showDodgersByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "offset", defaultValue = "-1") int offsetDays) {

        LocalDate base = (date != null) ? date : decideTargetDateWithCutoff(JST, CUTOFF);
        LocalDate target = base.plusDays(offsetDays);

        var pkOpt = lineupService.findGamePkByDate(DODGERS_ID, target);
        if (pkOpt.isEmpty()) {
            if (offsetDays < 0) {
                pkOpt = lineupService.findPrevGamePk(DODGERS_ID, target, 10);
            } else {
                pkOpt = lineupService.findNextGamePk(DODGERS_ID, target, 10);
            }
        }
        return pkOpt.isEmpty()
                ? "redirect:/games/no-game"
                : "redirect:/games/" + pkOpt.getAsLong() + "/lineups";
    }

    /** JST 18:00 cutoff: before -> today, after -> tomorrow */
    private LocalDate decideTargetDateWithCutoff(ZoneId zone, LocalTime cutoff) {
        ZonedDateTime now = ZonedDateTime.now(zone);
        return now.toLocalTime().isBefore(cutoff)
                ? now.toLocalDate()
                : now.toLocalDate().plusDays(1);
    }

    // Home 埋め込み用（小型フラグメントを返す）
    @GetMapping("/widgets/dodgers/lineups")
    public String lineupWidget(
            @RequestParam(name = "offset", defaultValue = "-1") int offsetDays,
            Model model) {

        // /dodgers/lineups/auto と同じロジック
        var base = decideTargetDateWithCutoff(JST, CUTOFF);
        var target = base.plusDays(offsetDays);

        var pkOpt = lineupService.findGamePkByDate(DODGERS_ID, target);
        if (pkOpt.isEmpty()) {
            pkOpt = (offsetDays < 0)
                    ? lineupService.findPrevGamePk(DODGERS_ID, target, 10)
                    : lineupService.findNextGamePk(DODGERS_ID, target, 10);
        }

        if (pkOpt.isEmpty()) {
            // 取れなかった場合でもフラグメント自体は返す
            model.addAttribute("home", null);
            model.addAttribute("away", null);
            model.addAttribute("gameInfo", null);
            return "fragments/lineups_widget :: widget";
        }

        var res = lineupService.fetchLineups(pkOpt.getAsLong());
        model.addAttribute("home", res.home());
        model.addAttribute("away", res.away());
        model.addAttribute("gameInfo", res.gameInfo());
        return "fragments/lineups_widget :: widget";
    }

}
