package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.service.UserService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // 新規追加: / (ルート) エンドポイント
    @GetMapping("/")
    public String rootRedirect() {
        // ルートへのアクセスは常にログイン画面にリダイレクト
        return "redirect:/auth/login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup"; // src/main/resources/templates/signup.html
    }

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        if ("true".equals(error)) {
            model.addAttribute("errorMessage", "ユーザ名かパスワードが正しくありません");
        }
        if ("true".equals(logout)) {
            model.addAttribute("message", "ログアウトしました");
        }
        return "login"; // login.htmlを表示
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String username, @RequestParam String password, Model model) {
        System.out.println("DEBUG: signup endpoint called with username: " + username);

        try {
            // ユーザー名の重複チェック
            if (userService.isUsernameTaken(username)) {
                model.addAttribute("message", "ユーザー名はすでに使用されています。別のユーザー名を選択してください");
                return "signup"; // 登録ページに戻る
            }

            // ユーザー登録処理
            userService.saveUser(username, password);

            model.addAttribute("message", "ユーザー登録完了！");
            System.out.println("DEBUG: User successfully registered - username: " + username);
            return "signup-success";
        } catch (Exception e) {
            model.addAttribute("message", "予期しないエラーが発生しました。もう一度お試しください");
            e.printStackTrace();
            return "signup";
        }
    }

    @GetMapping("/role")
    public ResponseEntity<Map<String, String>> getUserRole(Authentication authentication) {
        System.out.println("DEBUG: /auth/role endpoint accessed");

        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("DEBUG: Unauthorized access to /auth/role");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        Map<String, String> response = new HashMap<>();
        response.put("role", role);

        System.out.println("DEBUG: User role fetched - role: " + role);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {
        System.out.println("DEBUG: login endpoint called with username: " + username);

        // ログイン処理を呼び出す
        boolean success = userService.authenticate(username, password);

        if (success) {
            System.out.println("DEBUG: Login successful for username: " + username);
            return ResponseEntity.ok("Login successful");
        } else {
            System.out.println("DEBUG: Login failed for username: " + username);
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }
}
