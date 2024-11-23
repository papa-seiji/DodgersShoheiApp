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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * コメントページの表示
     *
     * @param model Thymeleaf に渡すモデル
     * @return comments.html テンプレート
     */
    @GetMapping
    public String showCommentsPage(Model model) {
        List<Comment> comments = commentRepository.findAll();
        model.addAttribute("comments", comments); // コメントをモデルに追加
        return "comments"; // comments.html をレンダリング
    }

    /**
     * 新しいコメントを追加
     *
     * @param comment        リクエストボディに含まれるコメントデータ
     * @param authentication 現在の認証情報
     * @return 保存されたコメントまたはエラーのレスポンス
     */
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
        comment.setUser(user);
        comment.setUsername(user.getUsername());
        comment.setCreatedAt(LocalDateTime.now());

        // コメントを保存
        Comment savedComment = commentRepository.save(comment);

        // WebSocketで通知
        System.out.println("WebSocket通知データ: " + savedComment); // デバッグログ追加
        messagingTemplate.convertAndSend("/topic/comments", savedComment);

        return ResponseEntity.ok(savedComment);
    }
}
