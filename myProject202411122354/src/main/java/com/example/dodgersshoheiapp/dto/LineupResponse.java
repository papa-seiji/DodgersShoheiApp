package com.example.dodgersshoheiapp.dto;

import java.util.List;

public record LineupResponse(
        TeamLineup home,
        TeamLineup away,
        GameInfo gameInfo,
        List<Integer> homeRunsByInning,
        List<Integer> awayRunsByInning) {

    /**
     * 🔥 既存互換コンストラクタ（超重要）
     * 旧コードとの完全互換を維持するため必須
     */
    public LineupResponse(TeamLineup home, TeamLineup away, GameInfo gameInfo) {
        this(home, away, gameInfo, List.of(), List.of());
    }
}
