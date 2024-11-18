package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.Counter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CounterRepository extends JpaRepository<Counter, Long> {
    Optional<Counter> findByCounterName(String counterName);
}
