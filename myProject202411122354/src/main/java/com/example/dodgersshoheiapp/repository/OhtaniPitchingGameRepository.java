package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.OhtaniPitchingGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OhtaniPitchingGameRepository
        extends JpaRepository<OhtaniPitchingGame, Long> {

    List<OhtaniPitchingGame> findByGameDateBetween(
            LocalDate startDate,
            LocalDate endDate);
}
