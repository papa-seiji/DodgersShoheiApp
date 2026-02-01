package com.example.dodgersshoheiapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OhtaniGame {

    private int id;
    private LocalDate gameDate;
    private String opponent;
    private String result;
    private int formValue;
    private LocalDateTime createdAt;

    // ★ これが足りなかった
    private List<OhtaniGameDetail> details;

    // ===== Getter / Setter =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getFormValue() {
        return formValue;
    }

    public void setFormValue(int formValue) {
        this.formValue = formValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ★ これが Controller から呼ばれる
    public List<OhtaniGameDetail> getDetails() {
        return details;
    }

    public void setDetails(List<OhtaniGameDetail> details) {
        this.details = details;
    }
}
