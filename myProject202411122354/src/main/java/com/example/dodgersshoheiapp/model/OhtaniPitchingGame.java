package com.example.dodgersshoheiapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "ohtani_pitching_games")
public class OhtaniPitchingGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_date")
    private LocalDate gameDate;

    private String opponent;

    private String result;

    @Column(name = "form_value")
    private String formValue; // ← A / S / B / C / D

    private String comment;

    // ===== 表示用（DB非永続）=====
    @Transient
    private List<OhtaniPitchingGameDetail> details;

    // ===== getter / setter =====

    public Long getId() {
        return id;
    }

    public LocalDate getGameDate() {
        return gameDate;
    }

    public void setGameDate(LocalDate gameDate) {
        this.gameDate = gameDate;
    }

    public String getOpponent() {
        return opponent;
    }

    public void setOpponent(String opponent) {
        this.opponent = opponent;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getFormValue() {
        return formValue;
    }

    public void setFormValue(String formValue) {
        this.formValue = formValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<OhtaniPitchingGameDetail> getDetails() {
        return details;
    }

    public void setDetails(List<OhtaniPitchingGameDetail> details) {
        this.details = details;
    }
}
