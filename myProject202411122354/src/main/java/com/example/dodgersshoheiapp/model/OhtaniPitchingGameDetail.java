package com.example.dodgersshoheiapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ohtani_pitching_game_details")
public class OhtaniPitchingGameDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    // =====================
    // 勝敗・球数
    // =====================
    @Column(name = "win_lose")
    private String winLose;

    @Column(name = "decision_pitcher")
    private String decisionPitcher;

    @Column(name = "total_pitches")
    private Integer totalPitches;

    // =====================
    // 1回
    // =====================
    @Column(name = "inning1_so")
    private Integer inning1So;

    @Column(name = "inning1_bb")
    private Integer inning1Bb;

    @Column(name = "inning1_hit")
    private Integer inning1Hit;

    @Column(name = "inning1_out")
    private Integer inning1Out;

    @Column(name = "inning1_description", columnDefinition = "TEXT")
    private String inning1Description;

    // =====================
    // 2回
    // =====================
    @Column(name = "inning2_so")
    private Integer inning2So;

    @Column(name = "inning2_bb")
    private Integer inning2Bb;

    @Column(name = "inning2_hit")
    private Integer inning2Hit;

    @Column(name = "inning2_out")
    private Integer inning2Out;

    @Column(name = "inning2_description", columnDefinition = "TEXT")
    private String inning2Description;

    // =====================
    // 3回
    // =====================
    @Column(name = "inning3_so")
    private Integer inning3So;

    @Column(name = "inning3_bb")
    private Integer inning3Bb;

    @Column(name = "inning3_hit")
    private Integer inning3Hit;

    @Column(name = "inning3_out")
    private Integer inning3Out;

    @Column(name = "inning3_description", columnDefinition = "TEXT")
    private String inning3Description;

    // =====================
    // 4回
    // =====================
    @Column(name = "inning4_so")
    private Integer inning4So;

    @Column(name = "inning4_bb")
    private Integer inning4Bb;

    @Column(name = "inning4_hit")
    private Integer inning4Hit;

    @Column(name = "inning4_out")
    private Integer inning4Out;

    @Column(name = "inning4_description", columnDefinition = "TEXT")
    private String inning4Description;

    // =====================
    // 5回
    // =====================
    @Column(name = "inning5_so")
    private Integer inning5So;

    @Column(name = "inning5_bb")
    private Integer inning5Bb;

    @Column(name = "inning5_hit")
    private Integer inning5Hit;

    @Column(name = "inning5_out")
    private Integer inning5Out;

    @Column(name = "inning5_description", columnDefinition = "TEXT")
    private String inning5Description;

    // =====================
    // 6回
    // =====================
    @Column(name = "inning6_so")
    private Integer inning6So;

    @Column(name = "inning6_bb")
    private Integer inning6Bb;

    @Column(name = "inning6_hit")
    private Integer inning6Hit;

    @Column(name = "inning6_out")
    private Integer inning6Out;

    @Column(name = "inning6_description", columnDefinition = "TEXT")
    private String inning6Description;

    // =====================
    // 7回
    // =====================
    @Column(name = "inning7_so")
    private Integer inning7So;

    @Column(name = "inning7_bb")
    private Integer inning7Bb;

    @Column(name = "inning7_hit")
    private Integer inning7Hit;

    @Column(name = "inning7_out")
    private Integer inning7Out;

    @Column(name = "inning7_description", columnDefinition = "TEXT")
    private String inning7Description;

    // =====================
    // 8回
    // =====================
    @Column(name = "inning8_so")
    private Integer inning8So;

    @Column(name = "inning8_bb")
    private Integer inning8Bb;

    @Column(name = "inning8_hit")
    private Integer inning8Hit;

    @Column(name = "inning8_out")
    private Integer inning8Out;

    @Column(name = "inning8_description", columnDefinition = "TEXT")
    private String inning8Description;

    // =====================
    // 9回
    // =====================
    @Column(name = "inning9_so")
    private Integer inning9So;

    @Column(name = "inning9_bb")
    private Integer inning9Bb;

    @Column(name = "inning9_hit")
    private Integer inning9Hit;

    @Column(name = "inning9_out")
    private Integer inning9Out;

    @Column(name = "inning9_description", columnDefinition = "TEXT")
    private String inning9Description;

    // =====================
    // 10回
    // =====================
    @Column(name = "inning10_so")
    private Integer inning10So;

    @Column(name = "inning10_bb")
    private Integer inning10Bb;

    @Column(name = "inning10_hit")
    private Integer inning10Hit;

    @Column(name = "inning10_out")
    private Integer inning10Out;

    @Column(name = "inning10_description", columnDefinition = "TEXT")
    private String inning10Description;

    // =====================
    // 共通
    // =====================
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =====================
    // lifecycle
    // =====================
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // =====================
    // getter / setter
    // =====================
    public Long getId() {
        return id;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getWinLose() {
        return winLose;
    }

    public String getDecisionPitcher() {
        return decisionPitcher;
    }

    public Integer getTotalPitches() {
        return totalPitches;
    }

    public Integer getInning1So() {
        return inning1So;
    }

    public Integer getInning1Bb() {
        return inning1Bb;
    }

    public Integer getInning1Hit() {
        return inning1Hit;
    }

    public Integer getInning1Out() {
        return inning1Out;
    }

    public String getInning1Description() {
        return inning1Description;
    }

    public Integer getInning2So() {
        return inning2So;
    }

    public Integer getInning2Bb() {
        return inning2Bb;
    }

    public Integer getInning2Hit() {
        return inning2Hit;
    }

    public Integer getInning2Out() {
        return inning2Out;
    }

    public String getInning2Description() {
        return inning2Description;
    }

    public Integer getInning3So() {
        return inning3So;
    }

    public Integer getInning3Bb() {
        return inning3Bb;
    }

    public Integer getInning3Hit() {
        return inning3Hit;
    }

    public Integer getInning3Out() {
        return inning3Out;
    }

    public String getInning3Description() {
        return inning3Description;
    }

    public Integer getInning4So() {
        return inning4So;
    }

    public Integer getInning4Bb() {
        return inning4Bb;
    }

    public Integer getInning4Hit() {
        return inning4Hit;
    }

    public Integer getInning4Out() {
        return inning4Out;
    }

    public String getInning4Description() {
        return inning4Description;
    }

    public Integer getInning5So() {
        return inning5So;
    }

    public Integer getInning5Bb() {
        return inning5Bb;
    }

    public Integer getInning5Hit() {
        return inning5Hit;
    }

    public Integer getInning5Out() {
        return inning5Out;
    }

    public String getInning5Description() {
        return inning5Description;
    }

    public Integer getInning6So() {
        return inning6So;
    }

    public Integer getInning6Bb() {
        return inning6Bb;
    }

    public Integer getInning6Hit() {
        return inning6Hit;
    }

    public Integer getInning6Out() {
        return inning6Out;
    }

    public String getInning6Description() {
        return inning6Description;
    }

    public Integer getInning7So() {
        return inning7So;
    }

    public Integer getInning7Bb() {
        return inning7Bb;
    }

    public Integer getInning7Hit() {
        return inning7Hit;
    }

    public Integer getInning7Out() {
        return inning7Out;
    }

    public String getInning7Description() {
        return inning7Description;
    }

    public Integer getInning8So() {
        return inning8So;
    }

    public Integer getInning8Bb() {
        return inning8Bb;
    }

    public Integer getInning8Hit() {
        return inning8Hit;
    }

    public Integer getInning8Out() {
        return inning8Out;
    }

    public String getInning8Description() {
        return inning8Description;
    }

    public Integer getInning9So() {
        return inning9So;
    }

    public Integer getInning9Bb() {
        return inning9Bb;
    }

    public Integer getInning9Hit() {
        return inning9Hit;
    }

    public Integer getInning9Out() {
        return inning9Out;
    }

    public String getInning9Description() {
        return inning9Description;
    }

    public Integer getInning10So() {
        return inning10So;
    }

    public Integer getInning10Bb() {
        return inning10Bb;
    }

    public Integer getInning10Hit() {
        return inning10Hit;
    }

    public Integer getInning10Out() {
        return inning10Out;
    }

    public String getInning10Description() {
        return inning10Description;
    }

}
