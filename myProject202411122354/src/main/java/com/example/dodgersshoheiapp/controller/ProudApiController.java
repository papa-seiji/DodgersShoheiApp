package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.ProudImage;
import com.example.dodgersshoheiapp.service.ProudService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/proud")
public class ProudApiController {

    private final ProudService proudService;
    private final SimpMessagingTemplate messagingTemplate;

    public ProudApiController(ProudService proudService, SimpMessagingTemplate messagingTemplate) {
        this.proudService = proudService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(
            @RequestParam("image") MultipartFile file,
            @RequestParam("description") String description,
            @RequestParam("createdBy") String createdBy) {
        try {
            ProudImage savedImage = proudService.saveImage(file, description, createdBy);

            // WebSocketで通知
            messagingTemplate.convertAndSend("/topic/newImage", savedImage);

            return ResponseEntity.ok("Image uploaded successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading image: " + e.getMessage());
        }
    }

    @GetMapping("/images")
    public ResponseEntity<List<ProudImage>> getAllImages() {
        return ResponseEntity.ok(proudService.getAllImages());
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("src/main/resources/static/uploads").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header("Content-Type", Files.probeContentType(filePath)) // MIMEタイプを動的に設定
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
