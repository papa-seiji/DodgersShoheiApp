package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.WbcTournamentMatch;
import com.example.dodgersshoheiapp.repository.WbcTournamentMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WbcMatchAdminFragmentController {

    private final WbcTournamentMatchRepository repository;

    /**
     * WBC MATCHES 管理用 Fragment
     * ・htmx 埋め込み専用
     * ・ROLE_ADMIN のみアクセス可
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/wbc/fragment/matches")
    public String wbcMatchesFragment(
            @RequestParam(defaultValue = "2026") int year,
            Model model) {

        List<WbcTournamentMatch> matches = repository.findByYearOrderByRoundAscMatchNoAsc(year);

        model.addAttribute("year", year);
        model.addAttribute("round", "ALL");
        model.addAttribute("matches", matches);

        // チーム一覧（既存仕様に完全準拠）
        model.addAttribute("teamList", List.of(
                "PUERTO RICO", "CUBA", "CANADA", "PANAMA", "COLOMBIA",
                "USA", "MEXICO", "ITALY", "UNITED KINGDOM", "BRAZIL",
                "JAPAN", "AUSTRALIA", "KOREA", "CZECH REPUBLIC", "CHINESE TAIPEI",
                "VENEZUELA", "DOMINICAN REPUBLIC", "NETHERLANDS", "ISRAEL", "NICARAGUA"));

        // ★ htmx 用 fragment を返す（ここが最重要）
        return "admin/wbc_matches_fragment :: wbcMatchesAdmin";
    }
}
