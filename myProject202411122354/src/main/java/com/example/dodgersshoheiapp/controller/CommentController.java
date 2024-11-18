package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.Comment;
import com.example.dodgersshoheiapp.model.User;
import com.example.dodgersshoheiapp.repository.CommentRepository;
import com.example.dodgersshoheiapp.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // コメントをすべて取得
    @GetMapping
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    // 新しいコメントを追加
    @PostMapping("/add")
    public ResponseEntity<Comment> addComment(@RequestBody Comment comment, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 認証ユーザー情報を取得
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> userOptional = userRepository.findByUsername(userDetails.getUsername());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userOptional.get();

        // コメントにユーザー情報をセット
        comment.setUser(user); // ユーザーエンティティの関連付け
        comment.setUsername(user.getUsername()); // `comments`テーブルの`username`カラムに登録
        comment.setCreatedAt(LocalDateTime.now()); // 作成日時を設定

        // コメントを保存
        Comment savedComment = commentRepository.save(comment);

        // WebSocketで通知
        messagingTemplate.convertAndSend("/topic/comments", savedComment);

        return ResponseEntity.ok(savedComment);
    }
}
