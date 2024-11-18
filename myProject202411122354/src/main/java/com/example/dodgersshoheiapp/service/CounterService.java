package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.model.Counter;
import com.example.dodgersshoheiapp.repository.CounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CounterService {

    @Autowired
    private CounterRepository counterRepository;

    public Counter updateCounterValue(String counterName, int increment) {
        // Optionalから値を取得
        Counter counter = counterRepository.findByCounterName(counterName)
                .orElseThrow(() -> new IllegalArgumentException("Counter not found: " + counterName));

        // カウンターの値を更新
        counter.setValue(counter.getValue() + increment);
        counter.setUpdatedAt(LocalDateTime.now());

        // 保存して更新されたカウンターを返す
        return counterRepository.save(counter);
    }

    public Counter getCounterByName(String counterName) {
        // Optionalから値を取得
        return counterRepository.findByCounterName(counterName)
                .orElseThrow(() -> new IllegalArgumentException("Counter not found: " + counterName));
    }
}
