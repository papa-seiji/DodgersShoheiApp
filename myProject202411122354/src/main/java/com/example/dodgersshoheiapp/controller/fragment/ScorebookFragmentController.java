package com.example.dodgersshoheiapp.controller.fragment;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/fragments")
public class ScorebookFragmentController {

    /**
     * BATTING 直近評価カード（home.html 用）
     */
    @GetMapping("/batting2026")
    public String batting2026() {
        return "fragments/Batting2026_ScoreBook";
    }

    /**
     * PITCHING 直近評価カード（home.html 用）
     */
    @GetMapping("/pitching2026")
    public String pitching2026() {
        return "fragments/Pitching2026_ScoreBook";
    }
}
