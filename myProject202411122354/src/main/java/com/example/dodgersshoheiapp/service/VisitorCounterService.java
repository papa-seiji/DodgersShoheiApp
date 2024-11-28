package com.example.dodgersshoheiapp.service;

import java.time.LocalDateTime;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.dodgersshoheiapp.model.VisitorCounter;
import com.example.dodgersshoheiapp.repository.VisitorCounterRepository;

import jakarta.transaction.Transactional;

@Service
public class VisitorCounterService {

    private final VisitorCounterRepository visitorCounterRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // コンストラクタインジェクション
    public VisitorCounterService(VisitorCounterRepository visitorCounterRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.visitorCounterRepository = visitorCounterRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public int incrementVisitorCounter() {
        VisitorCounter visitorCounter = visitorCounterRepository.findById(1)
                .orElse(new VisitorCounter(1, 0));
        visitorCounter.setValue(visitorCounter.getValue() + 1);
        visitorCounterRepository.save(visitorCounter);

        // WebSocket通知
        messagingTemplate.convertAndSend("/topic/visitorCounter", visitorCounter.getValue());

        return visitorCounter.getValue();
    }

    public int getCurrentValue() {
        return visitorCounterRepository.findById(1)
                .map(VisitorCounter::getValue)
                .orElse(0);
    }

    // エイリアスメソッド
    @Transactional
    public int incrementCounter() {
        return incrementVisitorCounter();
    }
}
