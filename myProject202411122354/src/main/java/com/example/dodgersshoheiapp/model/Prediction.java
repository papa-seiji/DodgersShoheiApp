package com.example.dodgersshoheiapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "mlb_predictions")
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String predictionType; // ここがNULLにならないようにする

    private String userId;
    private String sessionId;
    private String teamId;
    private String teamName;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
