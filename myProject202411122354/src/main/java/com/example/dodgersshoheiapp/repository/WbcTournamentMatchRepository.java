package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.WbcTournamentMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WbcTournamentMatchRepository
        extends JpaRepository<WbcTournamentMatch, Long> {

    // round別取得（match_no順）
    List<WbcTournamentMatch> findByYearAndRoundOrderByMatchNo(int year, String round);

    // round初期化用（切り戻し安全）
    void deleteByYearAndRound(int year, String round);
}
