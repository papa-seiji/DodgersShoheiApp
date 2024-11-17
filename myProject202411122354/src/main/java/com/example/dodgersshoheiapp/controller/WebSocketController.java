package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.Comment;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/comments") // クライアントから受信
    @SendTo("/topic/comments") // 他のクライアントに送信
    public Comment broadcastComment(Comment comment) {
        return comment; // 全クライアントに送信
    }
}
