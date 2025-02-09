package com.example.dodgersshoheiapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "dodgers_wins")
public class DodgersWins {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // 🎯 prediction_type の NULL を防ぐ
    private String predictionType = "DODGERS_WINS"; // 🚀 デフォルト値をセット

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String winRange;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
