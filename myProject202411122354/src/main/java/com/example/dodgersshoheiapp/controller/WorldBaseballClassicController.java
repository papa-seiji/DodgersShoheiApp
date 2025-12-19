package com.example.dodgersshoheiapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WorldBaseballClassicController {

    @GetMapping("/WorldBaseballClassic")
    public String showWBCPage() {
        return "WorldBaseballClassic"; // ← HTMLファイル名（拡張子なし）
    }
}
