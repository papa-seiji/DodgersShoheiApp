package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.Comment;
import com.example.dodgersshoheiapp.repository.CommentRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentRepository commentRepository;

    public CommentController(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @GetMapping
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }
}
