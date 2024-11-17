package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final CommentService commentService;

    // コンストラクタで `CommentService` を注入
    @Autowired
    public HomeController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/home")
    public String handleComment(@RequestParam(required = false) String content, Model model) {
        if (content != null && !content.isEmpty()) {
            // ログイン中のユーザー名を仮に "testUser" とする（後で認証情報を使う）
            String username = "testUser"; // この部分は後で認証情報に置き換え可能
            commentService.saveComment(content, username);
        }

        // コメントリストをモデルに追加
        model.addAttribute("comments", commentService.getAllComments());
        return "home"; // "home.html" を表示
    }
}
