package com.example.dodgersshoheiapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class KikeHernandezController {

    @GetMapping("/kike")
    public String showKikeHernandezPage() {
        return "kike_hernandez"; // resources/templates/kike_hernandez.html をレンダリング
    }
}
