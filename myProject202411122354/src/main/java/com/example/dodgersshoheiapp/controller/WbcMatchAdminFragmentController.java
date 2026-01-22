package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.WbcTournamentMatch;
import com.example.dodgersshoheiapp.repository.WbcTournamentMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
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

        // DB取得（順序はここでは気にしない）
        List<WbcTournamentMatch> matches = repository.findByYearOrderByRoundAscMatchNoAsc(year);

        /*
         * =========================
         * ★ 管理画面用 表示順制御（重要）
         * 表示順：
         * 1. FINAL
         * 2. SF（matchNo 逆順）
         * 3. QF（matchNo 逆順）
         * =========================
         */
        matches.sort(
                Comparator
                        .comparingInt((WbcTournamentMatch m) -> {
                            return switch (m.getRound()) {
                                case "FINAL" -> 1;
                                case "SF" -> 2;
                                case "QF" -> 3;
                                default -> 99;
                            };
                        })
                        // SF / QF は matchNo を逆順表示
                        .thenComparing(m -> -m.getMatchNo()));

        model.addAttribute("year", year);
        model.addAttribute("round", "ALL");
        model.addAttribute("matches", matches);

        // チーム一覧（既存仕様に完全準拠）
        model.addAttribute("teamList", List.of(
                "PUERTO RICO", "CUBA", "CANADA", "PANAMA", "COLOMBIA",
                "USA", "MEXICO", "ITALY", "UNITED KINGDOM", "BRAZIL",
                "JAPAN", "AUSTRALIA", "KOREA", "CZECH REPUBLIC", "CHINESE TAIPEI",
                "VENEZUELA", "DOMINICAN REPUBLIC", "NETHERLANDS", "ISRAEL", "NICARAGUA"));

        // ★ htmx 用 fragment を返す（最重要）
        return "admin/wbc_matches_fragment :: wbcMatchesAdmin";
    }
}
