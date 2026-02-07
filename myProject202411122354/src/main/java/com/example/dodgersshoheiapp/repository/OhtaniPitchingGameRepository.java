package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.OhtaniPitchingGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OhtaniPitchingGameRepository
        extends JpaRepository<OhtaniPitchingGame, Long> {

    @Query("""
                SELECT g
                FROM OhtaniPitchingGame g
                WHERE g.gameDate BETWEEN :startDate AND :endDate
                ORDER BY g.gameDate ASC
            """)
    List<OhtaniPitchingGame> findByMonthRange(
            LocalDate startDate,
            LocalDate endDate);
}
