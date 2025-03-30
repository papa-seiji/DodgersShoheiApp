package com.example.dodgersshoheiapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OhtaniJudgeController {

    @GetMapping("/ohtani-vs-judge")
    public String showOhtaniVsJudgePage() {
        return "ohtani_vs_judge";  // ← 拡張子はつけない。templates配下をSpringが自動解決
    }
}
