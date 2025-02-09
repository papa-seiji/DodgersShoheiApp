package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.DodgersWins;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DodgersWinsRepository extends JpaRepository<DodgersWins, Long> {
}
