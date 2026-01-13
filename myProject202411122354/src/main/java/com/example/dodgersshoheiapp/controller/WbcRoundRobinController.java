package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.dto.CellDto;
import com.example.dodgersshoheiapp.dto.TeamStatDto;
import com.example.dodgersshoheiapp.model.WbcPoolMatch;
import com.example.dodgersshoheiapp.service.WbcPoolMatchService;
import com.example.dodgersshoheiapp.service.WbcRoundRobinService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class WbcRoundRobinController {

    private final WbcPoolMatchService matchService;
    private final WbcRoundRobinService roundRobinService;

    public WbcRoundRobinController(
            WbcPoolMatchService matchService,
            WbcRoundRobinService roundRobinService) {
        this.matchService = matchService;
        this.roundRobinService = roundRobinService;
    }

    @GetMapping("/wbc/roundrobin")
    public String showRoundRobin(
            @RequestParam(required = false, defaultValue = "C") String pool,
            Model model) {

        int year = 2026;

        List<WbcPoolMatch> matches = matchService.getMatchesByYearAndPool(year, pool);

        // ★★★ ここが決定打 ★★★
        List<String> teams = roundRobinService.getFixedTeams(pool)
                .stream()
                .map(String::trim) // normalize
                .toList();

        Map<String, Map<String, CellDto>> matrix = roundRobinService.createEmptyMatrix(teams);

        roundRobinService.applyMatchesToMatrix(matches, matrix);

        model.addAttribute("teams", teams);
        model.addAttribute("matrix", matrix);

        Map<String, TeamStatDto> stats = roundRobinService.calculateTeamStats(teams, matrix);

        model.addAttribute("stats", stats);

        return "wbc2026_roundrobin_all";
    }
}