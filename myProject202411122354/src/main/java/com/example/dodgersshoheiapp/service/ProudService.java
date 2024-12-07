package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.model.ProudImage;
import com.example.dodgersshoheiapp.repository.ProudImageRepository;
import org.apache.commons.io.FilenameUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class ProudService {

    private static final Logger LOGGER = Logger.getLogger(ProudService.class.getName());
    private final ProudImageRepository proudImageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ProudService(ProudImageRepository proudImageRepository, SimpMessagingTemplate messagingTemplate) {
        this.proudImageRepository = proudImageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public ProudImage saveImage(MultipartFile file, String description, String createdBy) {
        String uploadDir = "src/main/resources/static/uploads/";
        Path uploadPath = Paths.get(uploadDir);

        try {
            LOGGER.info("Checking upload directory: " + uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                LOGGER.info("Upload directory created.");
            }

            String originalFileName = file.getOriginalFilename();
            String fileExtension = FilenameUtils.getExtension(originalFileName);
            String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;

            Path filePath = uploadPath.resolve(uniqueFileName);
            LOGGER.info("Saving file to: " + filePath);
            Files.copy(file.getInputStream(), filePath);

            ProudImage image = new ProudImage();
            image.setImageUrl("/api/proud/files/" + uniqueFileName);
            image.setDescription(description);
            image.setCreatedBy(createdBy);
            image.setCreatedAt(LocalDateTime.now());

            ProudImage savedImage = proudImageRepository.save(image);
            LOGGER.info("Image saved to database with ID: " + savedImage.getId());

            messagingTemplate.convertAndSend("/topic/proudGallery", savedImage);
            LOGGER.info("WebSocket notification sent for image ID: " + savedImage.getId());

            return savedImage;

        } catch (IOException e) {
            LOGGER.severe("Error saving file: " + e.getMessage());
            throw new RuntimeException("Failed to save image: " + e.getMessage(), e);
        }
    }

    public List<ProudImage> getAllImages() {
        LOGGER.info("Fetching all images from the database.");
        return proudImageRepository.findAll();
    }

    public ProudImage toggleLike(Long imageId, String username) {
        LOGGER.info("Processing like toggle for image ID: " + imageId + " by user: " + username);

        ProudImage image = proudImageRepository.findById(imageId)
                .orElseThrow(() -> {
                    LOGGER.severe("Image not found with ID: " + imageId);
                    return new RuntimeException("Image not found");
                });

        List<String> likedUsers = Arrays.stream(image.getLikedBy().split(","))
                .filter(user -> !user.isBlank())
                .collect(Collectors.toList());

        if (likedUsers.contains(username)) {
            likedUsers.remove(username);
            image.setLikesCount(image.getLikesCount() - 1);
            LOGGER.info("User " + username + " unliked image ID " + imageId + ". New likes count: "
                    + image.getLikesCount());
        } else {
            likedUsers.add(username);
            image.setLikesCount(image.getLikesCount() + 1);
            LOGGER.info(
                    "User " + username + " liked image ID " + imageId + ". New likes count: " + image.getLikesCount());
        }

        image.setLikedBy(String.join(",", likedUsers));
        ProudImage updatedImage = proudImageRepository.save(image);

        LOGGER.info("Updated likes count for image ID " + imageId + ": " + updatedImage.getLikesCount());

        messagingTemplate.convertAndSend("/topic/proudLikes", updatedImage);
        LOGGER.info("WebSocket message sent for likes update on image ID: " + imageId);

        return updatedImage;
    }
}
