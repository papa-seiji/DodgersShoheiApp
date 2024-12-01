package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.model.ProudImage;
import com.example.dodgersshoheiapp.model.ProudComment;
import com.example.dodgersshoheiapp.repository.ProudImageRepository;
import com.example.dodgersshoheiapp.repository.ProudCommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProudService {
    private final ProudImageRepository imageRepository;
    private final ProudCommentRepository commentRepository;

    public ProudService(ProudImageRepository imageRepository, ProudCommentRepository commentRepository) {
        this.imageRepository = imageRepository;
        this.commentRepository = commentRepository;
    }

    public List<ProudImage> getAllImages() {
        return imageRepository.findAll();
    }

    public ProudImage saveImage(ProudImage image) {
        return imageRepository.save(image);
    }

    public List<ProudComment> getCommentsByImageId(Long imageId) {
        return commentRepository.findByImageId(imageId);
    }

    public ProudComment saveComment(ProudComment comment) {
        return commentRepository.save(comment);
    }
}
