package com.example.dodgersshoheiapp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HeaderController {

    @GetMapping("/auth/userinfo")
    public Map<String, String> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // ログイン時にランダムな番号（1〜9）を割り当て
        int randomNumber = (int) (Math.random() * 9) + 1;

        // ログイン時にランダムなポジションを割り当て
        String[] positions = { "P", "C", "1B", "2B", "3B", "SS", "LF", "CF", "RF", "DH" };
        String position = positions[(int) (Math.random() * positions.length)];

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", username);
        userInfo.put("number", String.valueOf(randomNumber));
        userInfo.put("position", position);

        return userInfo;
    }
}
