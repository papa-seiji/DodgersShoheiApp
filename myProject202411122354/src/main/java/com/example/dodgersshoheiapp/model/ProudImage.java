package com.example.dodgersshoheiapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ProudImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    private String description;

    private String createdBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and Setters
}
