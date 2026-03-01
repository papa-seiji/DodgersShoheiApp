package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.service.MLBGameService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mlb")
@CrossOrigin(origins = "https://letsgotohtanifromjapan.click") // 本番環境のドメインを許可
public class MLBGameController {

    private final MLBGameService mlbGameService;

    public MLBGameController(MLBGameService mlbGameService) {
        this.mlbGameService = mlbGameService;
    }

    // ============================================
    // 試合情報取得
    // ============================================
    @GetMapping("/game")
    public Map<String, Object> getFormattedGame(@RequestParam String date) {
        return mlbGameService.getFormattedGameInfo(date);
    }

    // ============================================
    // PlayByPlay 確認用
    // ============================================
    @GetMapping("/test/playbyplay")
    @ResponseBody
    public Object testPlayByPlay(@RequestParam String date) {

        Long gamePk = mlbGameService.findGamePkByDate(date);

        if (gamePk == null) {
            return "gamePk not found";
        }

        return mlbGameService.getPlayByPlay(gamePk);
    }

    // ============================================
    // 対象日のHR数を取得する
    // ============================================
    @GetMapping("/test/hr")
    public Object testHr(@RequestParam String date) {

        Long gamePk = mlbGameService.findGamePkByDate(date);

        if (gamePk == null)
            return "gamePk not found";

        return mlbGameService.countHomeRuns(gamePk);
    }

    // ============================================
    // HRを打った選手名を取得する
    // ============================================
    @GetMapping("/test/hr/details")
    public Object testHrDetails(@RequestParam String date) {

        Long gamePk = mlbGameService.findGamePkByDate(date);
        if (gamePk == null)
            return "gamePk not found";

        return mlbGameService.getHomeRunHitters(gamePk);
    }

    // ============================================
    // HRの打球データを取得する
    // launchSpeed / launchAngle / totalDistance
    // ============================================
    @GetMapping("/test/hr/full")
    public Object testHrFull(@RequestParam String date) {

        Long gamePk = mlbGameService.findGamePkByDate(date);
        if (gamePk == null)
            return "gamePk not found";

        return mlbGameService.getHomeRunDetails(gamePk);
    }

    // ============================================
    // 🔥 Shohei専用HRデータ取得（今回追加）
    // ============================================
    @GetMapping("/test/hr/shohei")
    public Object testShoheiHr(@RequestParam String date) {

        Long gamePk = mlbGameService.findGamePkByDate(date);

        if (gamePk == null)
            return "gamePk not found";

        return mlbGameService.getShoheiHomeRuns(gamePk);
    }
}