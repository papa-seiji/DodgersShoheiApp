package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.NewsUpdate;
import com.example.dodgersshoheiapp.repository.NewsUpdateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

@Controller
@RequestMapping("/admin/news")
public class AdminNewsController {

    @Autowired
    private NewsUpdateRepository newsUpdateRepository;

    // 管理者専用のニュース管理画面を表示
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String showAdminNewsPage() {
        return "admin-news"; // templates/admin-news.html を返す
    }

    // ニュースを追加するエンドポイント
    @PostMapping("/add")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public String addNews(@RequestBody NewsUpdate newsUpdate) {
        newsUpdateRepository.save(newsUpdate);
        return "ニュースを追加しました!";
    }
}
