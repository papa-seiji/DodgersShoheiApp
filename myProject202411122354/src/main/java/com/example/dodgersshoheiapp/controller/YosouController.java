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

        // ✅ 投票データを登録
        @PostMapping("/vote")
        public ResponseEntity<Map<String, String>> saveVote(@RequestBody MlbYosouData voteData) {
            System.out.println("📝 投票データ受信: " + voteData);
            yosouService.saveVote(voteData);

            Map<String, String> response = new HashMap<>();
            response.put("message", "投票が成功しました");
            return ResponseEntity.ok(response);
        }

        // ✅ 予想データを取得（クエリパラメータを利用）
        @GetMapping("/data")
        public ResponseEntity<List<MlbYosouData>> getYosou(@RequestParam String yosouType) {
            System.out.println("🔍 予想データ取得リクエスト受信: " + yosouType);
            List<MlbYosouData> yosouList = yosouService.getYosouByType(yosouType);

            // ✅ 取得したデータをログ出力
            System.out.println("✅ 取得データのサイズ: " + yosouList.size());
            for (MlbYosouData data : yosouList) {
                System.out.println(
                        "✅ データ: " + data.getYosouType() + ", " + data.getYosouValue() + ", " + data.getVotedBy());
            }

            return ResponseEntity.ok(yosouList);
        }
    }
}
