package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.WbcTournamentMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WbcTournamentMatchRepository
        extends JpaRepository<WbcTournamentMatch, Long> {

    // =========================
    // round別取得（Canvas 用）
    // =========================
    List<WbcTournamentMatch> findByYearAndRoundOrderByMatchNo(int year, String round);

    // =========================
    // 年度全取得（Canvas / View 用）
    // ※ 文字列順（既存仕様・そのまま残す）
    // =========================
    List<WbcTournamentMatch> findByYearOrderByRoundAscMatchNoAsc(int year);

    // =========================
    // round初期化用
    // =========================
    void deleteByYearAndRound(int year, String round);

    // =========================
    // 管理画面用（単一試合取得）
    // =========================
    Optional<WbcTournamentMatch> findByYearAndRoundAndMatchNo(
            int year, String round, int matchNo);

    // =========================
    // ★ 管理画面用（論理順ソート）
    // QF → SF → FINAL
    // =========================
    @Query("""
            SELECT m FROM WbcTournamentMatch m
            WHERE m.year = :year
            ORDER BY
              CASE m.round
                WHEN 'QF' THEN 1
                WHEN 'SF' THEN 2
                WHEN 'FINAL' THEN 3
                ELSE 9
              END,
              m.matchNo
            """)
    List<WbcTournamentMatch> findByYearOrderByRoundLogical(
            @Param("year") int year);
}
