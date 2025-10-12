package com.example.dodgersshoheiapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class PostseasonController {

    @GetMapping("/postseason")
    public String showBracket(Model model) {
        Map<String, String> logos = Map.ofEntries(
                // AL
                Map.entry("TIGERS", "https://www.mlbstatic.com/team-logos/116.svg"),
                Map.entry("GUARDIANS", "https://www.mlbstatic.com/team-logos/114.svg"),
                Map.entry("MARINERS", "https://www.mlbstatic.com/team-logos/136.svg"),
                Map.entry("REDSOX", "https://www.mlbstatic.com/team-logos/111.svg"),
                Map.entry("YANKEES", "https://www.mlbstatic.com/team-logos/147.svg"),
                Map.entry("BLUEJAYS", "https://www.mlbstatic.com/team-logos/141.svg"),

                // NL
                Map.entry("REDS", "https://www.mlbstatic.com/team-logos/113.svg"),
                Map.entry("DODGERS", "https://www.mlbstatic.com/team-logos/119.svg"),
                Map.entry("PHILLIES", "https://www.mlbstatic.com/team-logos/143.svg"),
                Map.entry("PADRES", "https://www.mlbstatic.com/team-logos/135.svg"),
                Map.entry("CUBS", "https://www.mlbstatic.com/team-logos/112.svg"),
                Map.entry("BREWERS", "https://www.mlbstatic.com/team-logos/158.svg"));

        model.addAttribute("logos", logos);
        return "postseason";
    }
}
