package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.OhtaniPitchingGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface OhtaniPitchingGameRepository
                extends JpaRepository<OhtaniPitchingGame, Long> {

        // 月別取得（既存）
        List<OhtaniPitchingGame> findByGameDateBetween(
                        LocalDate start, LocalDate end);

        // ★ 追加：直近登板（PITTIN’ 用）
        OhtaniPitchingGame findTopByOrderByGameDateDesc();

        // ★ 既存：月取得を降順
        List<OhtaniPitchingGame> findByGameDateBetweenOrderByGameDateDesc(
                        LocalDate start, LocalDate end);

        // ★★★ 追加：月取得を昇順 ★★★
        List<OhtaniPitchingGame> findByGameDateBetweenOrderByGameDateAsc(
                        LocalDate start, LocalDate end);
}
