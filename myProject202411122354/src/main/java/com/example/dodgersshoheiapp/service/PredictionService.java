package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.model.Prediction;
import com.example.dodgersshoheiapp.repository.PredictionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PredictionService {

    private final PredictionRepository predictionRepository;

    public PredictionService(PredictionRepository predictionRepository) {
        this.predictionRepository = predictionRepository;
    }

    // ✅ 投票を保存
    public Prediction savePrediction(Prediction prediction) {
        return predictionRepository.save(prediction);
    }

    // ✅ 予想タイプ別の投票を取得
    public List<Prediction> getPredictions(String predictionType) {
        return predictionRepository.findByPredictionType(predictionType);
    }
}
