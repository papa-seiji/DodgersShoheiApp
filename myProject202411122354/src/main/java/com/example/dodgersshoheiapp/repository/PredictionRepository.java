package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    List<Prediction> findByPredictionType(String predictionType);
}
