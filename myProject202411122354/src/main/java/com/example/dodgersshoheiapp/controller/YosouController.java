package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.MlbYosouData;
import com.example.dodgersshoheiapp.service.YosouService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class YosouController {
    private final YosouService yosouService;

    public YosouController(YosouService yosouService) {
        this.yosouService = yosouService;
    }

    // ✅ 予想ページを表示 (Thymeleaf テンプレート)
    @GetMapping("/yosou")
    public String showYosouPage() {
        return "yosou_page"; // `src/main/resources/templates/yosou_page.html` がある前提
    }

    // ✅ API用のコントローラー
    @RestController
    @RequestMapping("/api/yosou")
    public static class YosouApiController {
        private final YosouService yosouService;

        public YosouApiController(YosouService yosouService) {
            this.yosouService = yosouService;
        }

        // ✅ 現在のログインユーザーを取得
        @GetMapping("/user")
        public ResponseEntity<Map<String, String>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
            Map<String, String> response = new HashMap<>();
            response.put("username", userDetails != null ? userDetails.getUsername() : "ゲスト");
            return ResponseEntity.ok(response);
        }

        // ✅ 投票データを登録・更新
        @PostMapping("/vote")
        public ResponseEntity<Map<String, String>> saveVote(@RequestBody MlbYosouData voteData) {
            System.out.println("📝 投票データ受信: " + voteData);
            yosouService.saveVote(voteData);

            Map<String, String> response = new HashMap<>();
            response.put("message", "投票が成功しました");
            return ResponseEntity.ok(response);
        }

        // ✅ 予想データを取得
        @GetMapping("/data")
        public ResponseEntity<List<MlbYosouData>> getYosou(@RequestParam String yosouType) {
            List<MlbYosouData> yosouList = yosouService.getYosouByType(yosouType);
            return ResponseEntity.ok(yosouList);
        }

        // ✅ ユーザーの現在の投票情報を取得
        @GetMapping("/user-vote")
        public ResponseEntity<MlbYosouData> getUserVote(
                @RequestParam String yosouType,
                @RequestParam String votedBy) {

            Optional<MlbYosouData> vote = yosouService.getUserVote(yosouType, votedBy);
            return vote.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.ok(null));
        }

        // ✅ データがOTANI_HR取得
        @GetMapping("/api/yosou/data")
        public List<MlbYosouData> getYosouData(@RequestParam String yosouType) {
            return yosouService.getYosouDataByType(yosouType);
        }

    }
}
