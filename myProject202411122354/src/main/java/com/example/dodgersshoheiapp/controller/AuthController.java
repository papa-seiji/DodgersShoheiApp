package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/signup")
    public String signupPage() {
        return "signup"; // src/main/resources/templates/signup.html
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // src/main/resources/templates/login.html
    }

    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<String> signup(@RequestParam String username, @RequestParam String password) {
        System.out.println("signup endpoint called with username: " + username); // デバッグログ

        // ユーザー登録処理
        userService.saveUser(username, password);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {
        System.out.println("login endpoint called with username: " + username); // デバッグログ

        // ログイン処理を呼び出す（詳細は省略）
        boolean success = userService.authenticate(username, password);

        if (success) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }
}
