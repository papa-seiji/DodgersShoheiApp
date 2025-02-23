package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.MlbApiLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MlbApiLogRepository extends JpaRepository<MlbApiLog, Long> {
}
