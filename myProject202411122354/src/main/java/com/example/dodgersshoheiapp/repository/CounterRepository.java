package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.Counter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounterRepository extends JpaRepository<Counter, Long> {
    Counter findByCounterName(String counterName);
}
