package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.service.WbcTournamentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.Map;

@RestController
@RequestMapping("/api/wbc/tournament")
public class WbcTournamentController {

    private final WbcTournamentService service;

    public WbcTournamentController(WbcTournamentService service) {
        this.service = service;
    }

    @PostMapping("/init/qf")
    public Map<String, String> initQuarterFinal(
            @RequestParam int year,
            @RequestParam String poolA1,
            @RequestParam String poolA2,
            @RequestParam String poolB1,
            @RequestParam String poolB2,
            @RequestParam String poolC1,
            @RequestParam String poolC2,
            @RequestParam String poolD1,
            @RequestParam String poolD2) {
        service.initQuarterFinal(
                year,
                poolA1, poolA2,
                poolB1, poolB2,
                poolC1, poolC2,
                poolD1, poolD2);
        return Map.of("status", "ok", "round", "QF");
    }

    // @GetMapping("/init/sf")
    @PostMapping("/init/sf")
    public Map<String, String> initSemiFinal(@RequestParam int year) {
        service.initSemiFinal(year);
        return Map.of("status", "ok", "round", "SF");
    }

    @PostMapping("/init/f")
    public Map<String, String> initFinal(@RequestParam int year) {
        service.initFinal(year);
        return Map.of("status", "ok", "round", "FINAL");
    }

    @PostMapping("/final/decide")
    public Map<String, String> decideChampion(
            @RequestParam int year,
            @RequestParam String winnerTeam) {
        service.decideChampion(year, winnerTeam);
        return Map.of("status", "ok", "champion", winnerTeam);
    }

    @GetMapping
    public List<com.example.dodgersshoheiapp.model.WbcTournamentMatch> getTournament(
            @RequestParam int year) {
        return service.getTournamentMatches(year);
    }

}
