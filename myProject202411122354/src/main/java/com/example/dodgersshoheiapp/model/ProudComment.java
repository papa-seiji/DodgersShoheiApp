package com.example.dodgersshoheiapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ProudComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "image_id", nullable = false)
    private ProudImage image;

    private String commentText;

    private String createdBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and Setters
}
