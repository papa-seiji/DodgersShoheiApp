package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.service.CommentService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final CommentService commentService;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket送信用

    // コンストラクタで `CommentService` と `SimpMessagingTemplate` を注入
    public HomeController(CommentService commentService, SimpMessagingTemplate messagingTemplate) {
        this.commentService = commentService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/home")
    public String handleComment(@RequestParam(required = false) String content, Model model) {
        if (content != null && !content.isEmpty()) {
            // ログイン中のユーザー名を仮に "testUser" とする（後で認証情報を使う）
            String username = "testUser"; // 認証情報に基づいたユーザー名を取得予定
            commentService.saveComment(content, username);

            // WebSocketを介してリアルタイムで新しいコメントを送信
            messagingTemplate.convertAndSend("/topic/comments", content);
        }

        // コメントリストをモデルに追加
        model.addAttribute("comments", commentService.getAllComments());
        return "home"; // "home.html" を表示
    }

    // 🎯 `yosou_page.html` を表示するルートを追加
    @GetMapping("/yosou")
    public String showYosouPage() {
        return "yosou_page"; // templates/yosou_page.html を表示
    }
}
