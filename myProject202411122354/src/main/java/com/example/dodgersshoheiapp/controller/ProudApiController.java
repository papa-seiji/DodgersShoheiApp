package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.ProudImage;
import com.example.dodgersshoheiapp.model.ProudComment;
import com.example.dodgersshoheiapp.service.ProudService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proud")
public class ProudApiController {
    private final ProudService proudService;

    public ProudApiController(ProudService proudService) {
        this.proudService = proudService;
    }

    @GetMapping("/images")
    public ResponseEntity<List<ProudImage>> getAllImages() {
        return ResponseEntity.ok(proudService.getAllImages());
    }

    @PostMapping("/images")
    public ResponseEntity<ProudImage> uploadImage(@RequestBody ProudImage image) {
        return ResponseEntity.ok(proudService.saveImage(image));
    }

    @GetMapping("/comments/{imageId}")
    public ResponseEntity<List<ProudComment>> getComments(@PathVariable Long imageId) {
        return ResponseEntity.ok(proudService.getCommentsByImageId(imageId));
    }

    @PostMapping("/comments")
    public ResponseEntity<ProudComment> postComment(@RequestBody ProudComment comment) {
        return ResponseEntity.ok(proudService.saveComment(comment));
    }
}
