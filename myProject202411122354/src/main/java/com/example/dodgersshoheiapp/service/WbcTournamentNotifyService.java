package com.example.dodgersshoheiapp.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WbcTournamentNotifyService {

    private final SimpMessagingTemplate messagingTemplate;

    public WbcTournamentNotifyService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyUpdated() {
        messagingTemplate.convertAndSend("/topic/wbc-tournament", "UPDATED");
    }
}
