package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.NewsUpdate;
import com.example.dodgersshoheiapp.repository.NewsUpdateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // ニュースを削除するエンドポイント
    @DeleteMapping("/delete/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteNews(@PathVariable Long id) {
        if (newsUpdateRepository.existsById(id)) {
            newsUpdateRepository.deleteById(id);
            return ResponseEntity.ok("ニュースを削除しました!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ニュースが見つかりませんでした");
        }
    }
}
