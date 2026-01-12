package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.WbcTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WbcTeamRepository extends JpaRepository<WbcTeam, Long> {

    List<WbcTeam> findByYearOrderByPoolAscTeamNameAsc(int year);
}
