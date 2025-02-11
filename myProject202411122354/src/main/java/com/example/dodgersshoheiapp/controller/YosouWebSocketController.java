package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.MlbYosouData;
import com.example.dodgersshoheiapp.service.YosouService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class YosouWebSocketController {

    private final YosouService yosouService;

    public YosouWebSocketController(YosouService yosouService) {
        this.yosouService = yosouService;
    }

    @MessageMapping("/vote")
    @SendTo("/topic/yosou")
    public MlbYosouData handleVote(MlbYosouData voteData) {
        yosouService.saveVote(voteData); // ✅ データをDBに保存
        return voteData; // ✅ クライアントにリアルタイム配信
    }
}
