package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.WbcTournamentMatch;
import com.example.dodgersshoheiapp.repository.WbcTournamentMatchRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/wbc")
public class WbcMatchAdminController {

    private final WbcTournamentMatchRepository repository;

    public WbcMatchAdminController(WbcTournamentMatchRepository repository) {
        this.repository = repository;
    }

    // =========================
    // 一覧表示（年度・ラウンド指定）
    // =========================
    @GetMapping("/matches")
    public String list(
            @RequestParam(defaultValue = "2026") int year,
            @RequestParam(required = false) String round,
            Model model) {

        List<WbcTournamentMatch> matches;

        if (round != null && !round.isBlank()) {
            matches = repository.findByYearAndRoundOrderByMatchNo(year, round);
        } else {
            // ★ ここを変更
            matches = repository.findByYearOrderByRoundLogical(year);
        }

        model.addAttribute("year", year);
        model.addAttribute("round", round);
        model.addAttribute("matches", matches);

        // ★ チームリスト（管理画面用）
        model.addAttribute("teamList", List.of(
                "PUERTO RICO", "CUBA", "CANADA", "PANAMA", "COLOMBIA",
                "USA", "MEXICO", "ITALY", "UNITED KINGDOM", "BRAZIL",
                "JAPAN", "AUSTRALIA", "KOREA", "CZECH REPUBLIC", "CHINESE TAIPEI",
                "VENEZUELA", "DOMINICAN REPUBLIC", "NETHERLANDS", "ISRAEL", "NICARAGUA"));

        return "admin/wbc_matches";
    }
}
