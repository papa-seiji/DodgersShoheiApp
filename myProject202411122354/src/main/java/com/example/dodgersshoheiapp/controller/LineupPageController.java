package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.dto.LineupResponse;
import com.example.dodgersshoheiapp.service.MlbLineupService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * ページ表示用。Serviceを直接呼ぶ。
 */
@Controller
public class LineupPageController {

    private final MlbLineupService lineupService;

    public LineupPageController(MlbLineupService lineupService) {
        this.lineupService = lineupService;
    }

    @GetMapping("/games/{gamePk}/lineups")
    public String showLineups(@PathVariable long gamePk, Model model) {
        LineupResponse res = lineupService.fetchLineups(gamePk);
        model.addAttribute("home", res.home());
        model.addAttribute("away", res.away());
        return "lineups"; // templates/lineups.html
    }
}
