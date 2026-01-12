package com.example.dodgersshoheiapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.dodgersshoheiapp.model.WbcPoolMatch;
import com.example.dodgersshoheiapp.service.WbcPoolMatchService;

@Controller
public class WorldBaseballClassicController {

    @Autowired
    private WbcPoolMatchService wbcPoolMatchService;

    /**
     * 🌍 World Baseball Classic 全体表示
     * 既存HTML（WorldBaseballClassic.html）を使用
     *
     * URL例:
     * /WorldBaseballClassic
     * /WorldBaseballClassic?year=2026
     */
    @GetMapping("/WorldBaseballClassic")
    public String showWBCPage(
            @RequestParam(name = "year", required = false, defaultValue = "2026") Integer year,
            Model model) {

        // 全POOL（A〜D）分の試合データを取得
        List<WbcPoolMatch> matches = wbcPoolMatchService.getAllMatchesByYear(year);

        model.addAttribute("year", year);
        model.addAttribute("matches", matches);

        return "WorldBaseballClassic"; // ← 既存HTML
    }
}
