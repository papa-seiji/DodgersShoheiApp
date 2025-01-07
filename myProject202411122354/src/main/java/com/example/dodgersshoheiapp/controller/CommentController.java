package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.Comment;
import com.example.dodgersshoheiapp.model.User;
import com.example.dodgersshoheiapp.model.Subscription;
import com.example.dodgersshoheiapp.repository.CommentRepository;
import com.example.dodgersshoheiapp.repository.UserRepository;
import com.example.dodgersshoheiapp.service.PushNotificationService;
import com.example.dodgersshoheiapp.repository.SubscriptionRepository;

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
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PushNotificationService pushNotificationService;

    @GetMapping
    public String showCommentsPage(Model model) {
        List<Comment> comments = commentRepository.findAll();
        model.addAttribute("comments", comments);
        return "comments";
    }

    @PostMapping("/add")
    public ResponseEntity<Comment> addComment(@RequestBody Comment comment, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> userOptional = userRepository.findByUsername(userDetails.getUsername());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userOptional.get();
        comment.setUser(user);
        comment.setUsername(user.getUsername());
        comment.setCreatedAt(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        System.out.println("WebSocket通知データ: " + savedComment);
        messagingTemplate.convertAndSend("/topic/comments", savedComment);

        List<Subscription> subscriptions = subscriptionRepository.findAll();
        for (Subscription subscription : subscriptions) {
            try {
                pushNotificationService.sendPushNotification(subscription, "新しいコメント",
                        savedComment.getUsername() + "さんがコメントを投稿しました！");
            } catch (Exception e) {
                System.err.println("プッシュ通知の送信に失敗しました: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(savedComment);
    }
}
