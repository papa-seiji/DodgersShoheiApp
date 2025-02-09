package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.MlbYosouData;
import com.example.dodgersshoheiapp.service.YosouService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/yosou")
public class YosouController {
    private final YosouService yosouService;

    public YosouController(YosouService yosouService) {
        this.yosouService = yosouService;
    }

    // ✅ 予想ページを表示
    @GetMapping
    public String showYosouPage() {
        return "yosou_page";
    }

    // ✅ API用のコントローラー
    @RestController
    @RequestMapping("/api/yosou") // ✅ `/api/yosou` に統一
    public static class YosouApiController {
        private final YosouService yosouService;

        public YosouApiController(YosouService yosouService) {
            this.yosouService = yosouService;
        }

        // ✅ 投票データを登録
        @PostMapping("/vote")
        public ResponseEntity<Map<String, String>> saveVote(@RequestBody MlbYosouData voteData) {
            System.out.println("📝 投票データ受信: " + voteData);
            yosouService.saveVote(voteData);

            Map<String, String> response = new HashMap<>();
            response.put("message", "投票が成功しました");
            return ResponseEntity.ok(response); // ✅ JSON形式で返す
        }

        // ✅ 予想データを取得（URLエンコード対応）
        @GetMapping("/{yosouType}")
        public ResponseEntity<List<MlbYosouData>> getYosou(@PathVariable String yosouType) {
            System.out.println("🔍 予想データ取得: " + yosouType);
            List<MlbYosouData> yosouList = yosouService.getYosouByType(yosouType);
            return ResponseEntity.ok(yosouList);
        }
    }
}
