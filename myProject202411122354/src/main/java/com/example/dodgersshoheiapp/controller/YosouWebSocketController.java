package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.MlbYosouData;
import com.example.dodgersshoheiapp.service.YosouService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class YosouWebSocketController {
    private final YosouService yosouService;

    public YosouWebSocketController(YosouService yosouService) {
        this.yosouService = yosouService;
    }

    // ✅ WebSocket 経由で投票を処理
    @MessageMapping("/vote")
    @SendTo("/topic/yosou")
    public List<MlbYosouData> handleVote(MlbYosouData voteData) {
        yosouService.saveVote(voteData);
        return yosouService.getYosouByType(voteData.getYosouType());
    }
}