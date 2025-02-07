package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.service.DodgersStandingsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dodgers/standings")
public class DodgersStandingsController {
    private final DodgersStandingsService standingsService;

    public DodgersStandingsController(DodgersStandingsService standingsService) {
        this.standingsService = standingsService;
    }

    @GetMapping
    public Map<String, List<Map<String, Object>>> getStandings() {
        return standingsService.getDodgersStandings(); // ← Map をそのまま返すように修正
    }
}
