package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.ProudImage;
import com.example.dodgersshoheiapp.service.ProudService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/proud")
public class ProudApiController {

    private final ProudService proudService;

    public ProudApiController(ProudService proudService) {
        this.proudService = proudService;
    }


    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(
            @RequestParam("image") MultipartFile file,
            @RequestParam("description") String description,
            @RequestParam("createdBy") String createdBy) {
        try {
            proudService.saveImage(file, description, createdBy);
            return ResponseEntity.ok("Image uploaded successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading image: " + e.getMessage());
        }
    }

    @PostMapping("/images")
    public ResponseEntity<ProudImage> uploadImage(@RequestBody ProudImage image) {
        return ResponseEntity.ok(proudService.saveImage(image));
    }
  

