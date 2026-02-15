package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.dto.LineupResponse;
import com.example.dodgersshoheiapp.service.MlbLineupService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

    /** フルページ：gamePk 固定表示 */
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

        // ✅ ここ追加（安全）
        model.addAttribute("homeRunsByInning", res.homeRunsByInning());
        model.addAttribute("awayRunsByInning", res.awayRunsByInning());

        return "lineups";
    }

    // ===============================
    // 🔥 DEBUG ENDPOINT（追加部分）
    // ===============================
    @GetMapping("/debug/lineup/{gamePk}")
    @ResponseBody
    public LineupResponse debugLineup(@PathVariable long gamePk) {

        System.out.println("===== DEBUG fetchLineups START =====");
        System.out.println("gamePk = " + gamePk);

        LineupResponse response = lineupService.fetchLineups(gamePk);

        System.out.println("===== DEBUG fetchLineups END =====");

        return response;
    }
    // ===============================

    @GetMapping("/dodgers/lineups/auto")
    public String showDodgersSmart(
            @RequestParam(name = "offset", defaultValue = "-1") int offsetDays) {

        LocalDate base = decideTargetDateWithCutoff(JST, CUTOFF);
        var todayOpt = lineupService.findGamePkByDate(DODGERS_ID, base);

        java.util.OptionalLong pkOpt;
        if (todayOpt.isEmpty()) {
            pkOpt = lineupService.findNextGamePk(DODGERS_ID, base, 14);
            if (pkOpt.isEmpty()) {
                pkOpt = lineupService.findPrevGamePk(DODGERS_ID, base, 14);
            }
        } else {
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

    @GetMapping("/dodgers/lineups")
    public String showDodgersByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "offset", defaultValue = "-1") int offsetDays) {

        LocalDate base = (date != null) ? date : decideTargetDateWithCutoff(JST, CUTOFF);
        LocalDate target = base.plusDays(offsetDays);

        var pkOpt = lineupService.findGamePkByDate(DODGERS_ID, target);
        if (pkOpt.isEmpty()) {
            pkOpt = lineupService.findNextGamePk(DODGERS_ID, target, 14);
            if (pkOpt.isEmpty()) {
                pkOpt = lineupService.findPrevGamePk(DODGERS_ID, target, 14);
            }
        }

        return pkOpt.isEmpty()
                ? "redirect:/games/no-game"
                : "redirect:/games/" + pkOpt.getAsLong() + "/lineups?left=away";
    }

    private LocalDate decideTargetDateWithCutoff(ZoneId zone, LocalTime cutoff) {
        ZonedDateTime now = ZonedDateTime.now(zone);
        return now.toLocalTime().isBefore(cutoff)
                ? now.toLocalDate()
                : now.toLocalDate().plusDays(1);
    }

    @GetMapping("/widgets/dodgers/lineups")
    public String lineupWidget(
            @RequestParam(name = "offset", defaultValue = "-1") int offsetDays,
            @RequestParam(name = "left", defaultValue = "away") String left,
            Model model) {

        LocalDate base = decideTargetDateWithCutoff(JST, CUTOFF);
        var todayOpt = lineupService.findGamePkByDate(DODGERS_ID, base);

        java.util.OptionalLong pkOpt;
        if (todayOpt.isEmpty()) {
            pkOpt = lineupService.findNextGamePk(DODGERS_ID, base, 14);
            if (pkOpt.isEmpty()) {
                pkOpt = lineupService.findPrevGamePk(DODGERS_ID, base, 14);
            }
        } else {
            LocalDate target = base.plusDays(offsetDays);
            pkOpt = lineupService.findGamePkByDate(DODGERS_ID, target);
            if (pkOpt.isEmpty()) {
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

        // ✅ ここ追加（安全）
        model.addAttribute("homeRunsByInning", res.homeRunsByInning());
        model.addAttribute("awayRunsByInning", res.awayRunsByInning());

        return "fragments/lineups_widget :: widget";

    }
}
