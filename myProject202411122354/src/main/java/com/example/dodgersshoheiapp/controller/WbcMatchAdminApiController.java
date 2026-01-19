package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.WbcTournamentMatch;
import com.example.dodgersshoheiapp.repository.WbcTournamentMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * WBC 試合管理（管理者用 API）
 * 画面遷移なし・JSON更新専用
 */
@RestController
@RequestMapping("/admin/wbc/api")
@RequiredArgsConstructor
public class WbcMatchAdminApiController {

    private final WbcTournamentMatchRepository repository;

    /**
     * 試合情報更新
     */
    @PostMapping("/match/update")
    public ResponseEntity<?> updateMatch(@RequestBody MatchUpdateRequest req) {

        Optional<WbcTournamentMatch> opt = repository.findById(req.getId());

        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        WbcTournamentMatch match = opt.get();

        // ★ 全項目を明示的に上書き（NULL事故防止）
        match.setHomeTeam(req.getHomeTeam());
        match.setAwayTeam(req.getAwayTeam());
        match.setHomeScore(req.getHomeScore());
        match.setAwayScore(req.getAwayScore());
        match.setWinnerTeam(req.getWinnerTeam());

        repository.save(match);

        // ★ JSON を返す（フロントの res.json() 対応）
        return ResponseEntity.ok(
                Map.of(
                        "result", "ok",
                        "id", match.getId()));
    }

    /**
     * 画面から受け取る JSON
     */
    public static class MatchUpdateRequest {

        private Long id;
        private String homeTeam;
        private String awayTeam;
        private Integer homeScore;
        private Integer awayScore;
        private String winnerTeam;

        // --- Getter / Setter ---

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getHomeTeam() {
            return homeTeam;
        }

        public void setHomeTeam(String homeTeam) {
            this.homeTeam = homeTeam;
        }

        public String getAwayTeam() {
            return awayTeam;
        }

        public void setAwayTeam(String awayTeam) {
            this.awayTeam = awayTeam;
        }

        public Integer getHomeScore() {
            return homeScore;
        }

        public void setHomeScore(Integer homeScore) {
            this.homeScore = homeScore;
        }

        public Integer getAwayScore() {
            return awayScore;
        }

        public void setAwayScore(Integer awayScore) {
            this.awayScore = awayScore;
        }

        public String getWinnerTeam() {
            return winnerTeam;
        }

        public void setWinnerTeam(String winnerTeam) {
            this.winnerTeam = winnerTeam;
        }
    }
}
