package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.dto.WbcPoolStandingDto;
import com.example.dodgersshoheiapp.model.WbcPoolMatch;
import com.example.dodgersshoheiapp.service.WbcPoolMatchService;
import com.example.dodgersshoheiapp.service.WbcPoolStandingService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
public class WorldBaseballClassicController {

    private final WbcPoolMatchService matchService;
    private final WbcPoolStandingService standingService;

    public WorldBaseballClassicController(
            WbcPoolMatchService matchService,
            WbcPoolStandingService standingService) {
        this.matchService = matchService;
        this.standingService = standingService;
    }

    @GetMapping("/WorldBaseballClassic")
    public String showWBCPage(Model model) {

        int year = 2026;

        // 🔹 POOLごとの参加チーム（固定）
        Map<String, List<String>> poolTeams = new LinkedHashMap<>();
        poolTeams.put("A", List.of("CANADA", "PANAMA", "COLOMBIA", "CUBA"));
        poolTeams.put("B", List.of("USA", "MEXICO", "ITALY", "UK"));
        poolTeams.put("C", List.of("JAPAN", "AUSTRALIA", "KOREA", "CHINA"));
        poolTeams.put("D", List.of("VENEZUELA", "DOMINICAN", "PUERTO RICO", "NETHERLANDS"));

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
