package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.MlbYosouData;
import com.example.dodgersshoheiapp.service.YosouService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller // ← RestController ではなく @Controller に変更
@RequestMapping("/yosou")
public class YosouController {
    private final YosouService yosouService;

    public YosouController(YosouService yosouService) {
        this.yosouService = yosouService;
    }

    // ✅ 予想ページを表示する
    @GetMapping
    public String showYosouPage() {
        return "yosou_page"; // templates/yosou_page.html をレンダリング
    }

    // ✅ 投票データを登録
    @PostMapping("/api/vote")
    @ResponseBody
    public ResponseEntity<String> saveVote(@RequestBody MlbYosouData voteData) {
        yosouService.saveVote(voteData);
        return ResponseEntity.ok("投票が成功しました");
    }

    // ✅ 予想データを取得
    @GetMapping("/api/{yosouType}")
    @ResponseBody
    public ResponseEntity<List<MlbYosouData>> getYosou(@PathVariable String yosouType) {
        return ResponseEntity.ok(yosouService.getYosouByType(yosouType));
    }
}