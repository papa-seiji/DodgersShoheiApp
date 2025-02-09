package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.Prediction;
import com.example.dodgersshoheiapp.model.DodgersWins;
import com.example.dodgersshoheiapp.service.PredictionService;
import com.example.dodgersshoheiapp.service.DodgersWinsService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class YosouController {

    private final PredictionService predictionService;
    private final DodgersWinsService dodgersWinsService;

    public YosouController(PredictionService predictionService, DodgersWinsService dodgersWinsService) {
        this.predictionService = predictionService;
        this.dodgersWinsService = dodgersWinsService;
    }

    // ✅ 西地区 1位予想データを登録
    @PostMapping("/predictions")
    public ResponseEntity<?> savePrediction(@RequestBody Prediction prediction) {
        if (prediction.getPredictionType() == null || prediction.getPredictionType().isEmpty()) {
            return ResponseEntity.badRequest().body("predictionType は必須です");
        }

        predictionService.savePrediction(prediction);
        return ResponseEntity.ok("保存成功");
    }

    // ✅ 西地区 1位予想データを取得
    @GetMapping("/predictions/{predictionType}")
    public List<Prediction> getPredictions(@PathVariable String predictionType) {
        return predictionService.getPredictions(predictionType);
    }

    // ✅ WebSocket経由の投票処理（西地区 1位予想）
    @MessageMapping("/vote")
    @SendTo("/topic/nl-west")
    public Map<String, Integer> handleVote(@RequestBody Map<String, String> voteData) {
        Map<String, Integer> voteCounts = new HashMap<>();

        if (voteData.containsKey("team1")) {
            voteCounts.put(voteData.get("team1"), 1);
        }
        if (voteData.containsKey("team2")) {
            voteCounts.put(voteData.get("team2"), 1);
        }

        return voteCounts;
    }

    // ✅ Dodgers 勝利数予想データを登録
    @PostMapping("/dodgers-wins")
    public ResponseEntity<?> saveDodgersWins(@RequestBody DodgersWins dodgersWins) {
        if (dodgersWins.getWinRange() == null || dodgersWins.getWinRange().isEmpty()) {
            return ResponseEntity.badRequest().body("winRange は必須です");
        }

        // 🎯 predictionType が NULL の場合、デフォルト値をセット
        if (dodgersWins.getPredictionType() == null) {
            dodgersWins.setPredictionType("DODGERS_WINS");
        }

        dodgersWinsService.saveDodgersWins(dodgersWins);
        return ResponseEntity.ok("保存成功");
    }

    // ✅ Dodgers 勝利数予想データを取得
    @GetMapping("/dodgers-wins")
    public List<DodgersWins> getDodgersWinsPredictions() {
        return dodgersWinsService.getAllDodgersWins();
    }

    // ✅ WebSocket経由の Dodgers 勝利数予想（リアルタイム同期）
    @MessageMapping("/dodgers-wins-vote")
    @SendTo("/topic/dodgers-wins")
    public Map<String, Integer> handleDodgersWinsVote(@RequestBody Map<String, String> voteData) {
        Map<String, Integer> winsCounts = new HashMap<>();

        if (voteData.containsKey("wins")) {
            winsCounts.put(voteData.get("wins"), 1);
        }

        return winsCounts;
    }
}
