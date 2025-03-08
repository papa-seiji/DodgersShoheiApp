package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.service.MLBGameService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mlb")
public class MLBGameController {
    private final MLBGameService mlbGameService;

    public MLBGameController(MLBGameService mlbGameService) {
        this.mlbGameService = mlbGameService;
    }

    @GetMapping("/game")
    public Map<String, Object> getFormattedGame(@RequestParam String date) {
        return mlbGameService.getFormattedGameInfo(date);
    }
}
