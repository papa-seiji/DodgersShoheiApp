package com.example.dodgersshoheiapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WbcRoundRobinController {

    @GetMapping("/wbc/roundrobin")
    public String showRoundRobin() {
        return "fragments/wbc2026_roundrobin_all";
    }
}
