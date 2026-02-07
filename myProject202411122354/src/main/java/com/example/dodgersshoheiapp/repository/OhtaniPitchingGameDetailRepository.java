package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.OhtaniPitchingGameDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OhtaniPitchingGameDetailRepository
        extends JpaRepository<OhtaniPitchingGameDetail, Long> {

    List<OhtaniPitchingGameDetail> findByGameId(Long gameId);
}
