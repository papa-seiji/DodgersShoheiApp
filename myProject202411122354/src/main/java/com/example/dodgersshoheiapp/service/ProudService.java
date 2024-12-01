package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.model.ProudImage;
import com.example.dodgersshoheiapp.repository.ProudImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProudService {

    private final ProudImageRepository proudImageRepository;

    public ProudService(ProudImageRepository proudImageRepository) {
        this.proudImageRepository = proudImageRepository;
    }

    public ProudImage saveImage(MultipartFile file, String description, String createdBy) {
        String uploadDir = "src/main/resources/static/uploads/";
        Path uploadPath = Paths.get(uploadDir);

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);// uploads フォルダを作成
            }

            String fileName = file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            ProudImage image = new ProudImage();
            image.setImageUrl("/uploads/" + fileName);
            image.setDescription(description);
            image.setCreatedBy(createdBy);
            image.setCreatedAt(LocalDateTime.now());

            return proudImageRepository.save(image);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + e.getMessage(), e);
        }
    }

    public List<ProudImage> getAllImages() {
        return proudImageRepository.findAll();
    }
}
