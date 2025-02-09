package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.MlbYosouData;
import com.example.dodgersshoheiapp.service.YosouService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/yosou")
public class YosouController {
    private final YosouService yosouService;

    public YosouController(YosouService yosouService) {
        this.yosouService = yosouService;
    }

    // ✅ 投票データを登録
    @PostMapping("/vote")
    public ResponseEntity<String> saveVote(@RequestBody MlbYosouData voteData) {
        yosouService.saveVote(voteData);
        return ResponseEntity.ok("投票が成功しました");
    }

    // ✅ 予想データを取得
    @GetMapping("/{yosouType}")
    public ResponseEntity<List<MlbYosouData>> getYosou(@PathVariable String yosouType) {
        return ResponseEntity.ok(yosouService.getYosouByType(yosouType));
    }
}
