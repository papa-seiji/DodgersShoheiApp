package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.WbcPoolMatch;
import com.example.dodgersshoheiapp.dto.WbcPoolStandingDto;
import com.example.dodgersshoheiapp.service.WbcPoolMatchService;
import com.example.dodgersshoheiapp.service.WbcPoolStandingService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

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

        // 🔹 確認用固定値（あとで動的にする）
        int year = 2026;
        String pool = "C"; // 全体表示だが、順位はPOOL単位で計算

        // 🔹 試合一覧（表①）
        List<WbcPoolMatch> matches = matchService.getMatchesByYearAndPool(year, pool);

        // 🔹 順位一覧（表②）
        List<WbcPoolStandingDto> standings = standingService.calculateStandings(matches);

        // 🔹 Model に詰める
        model.addAttribute("year", year);
        model.addAttribute("pool", pool);
        model.addAttribute("matches", matches);
        model.addAttribute("standings", standings);

        return "WorldBaseballClassic";
    }
}
