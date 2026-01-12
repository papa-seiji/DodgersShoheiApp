package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.dto.WbcPoolStandingDto;
import com.example.dodgersshoheiapp.model.WbcPoolMatch;
import com.example.dodgersshoheiapp.service.WbcPoolMatchService;
import com.example.dodgersshoheiapp.service.WbcPoolStandingService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.dodgersshoheiapp.service.WbcTeamService;

import java.util.*;

@Controller
public class WorldBaseballClassicController {

    private final WbcPoolMatchService matchService;
    private final WbcPoolStandingService standingService;
    private final WbcTeamService teamService; // ★追加

    public WorldBaseballClassicController(
            WbcPoolMatchService matchService,
            WbcPoolStandingService standingService,
            WbcTeamService teamService) { // ★追加
        this.matchService = matchService;
        this.standingService = standingService;
        this.teamService = teamService;
    }

    @GetMapping("/WorldBaseballClassic")
    public String showWBCPage(Model model) {

        int year = 2026;

        // ✅ DBから POOL別チーム取得
        Map<String, List<String>> poolTeams = teamService.getPoolTeamsByYear(year);

        Map<String, List<WbcPoolStandingDto>> poolStandings = new LinkedHashMap<>();

        for (String pool : poolTeams.keySet()) {

            List<WbcPoolMatch> matches = matchService.getMatchesByYearAndPool(year, pool);

            List<WbcPoolStandingDto> standings = standingService.calculateStandings(
                    year,
                    pool,
                    poolTeams.get(pool),
                    matches);

            poolStandings.put(pool, standings);
        }

        model.addAttribute("year", year);
        model.addAttribute("poolStandings", poolStandings);

        return "WorldBaseballClassic";
    }
}
