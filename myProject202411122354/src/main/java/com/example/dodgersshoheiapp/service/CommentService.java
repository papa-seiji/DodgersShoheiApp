package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.model.Comment;
import com.example.dodgersshoheiapp.model.User;
import com.example.dodgersshoheiapp.repository.CommentRepository;
import com.example.dodgersshoheiapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    public void saveComment(String content, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setUsername(user.getUsername());
        comment.setIcon(user.getIcon());
        comment.setCreatedAt(LocalDateTime.now());

        commentRepository.save(comment);
    }

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }
}
