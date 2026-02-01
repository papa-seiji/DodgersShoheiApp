package com.example.dodgersshoheiapp.model;

import java.time.LocalDateTime;

public class OhtaniGameDetail {

    private Integer id;
    private Integer gameId;
    private Integer battingOrder;
    private String pitcher;
    private String pitcherHand;
    private String result;
    private LocalDateTime createdAt;

    // ===== getter / setter =====

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public Integer getBattingOrder() {
        return battingOrder;
    }

    public void setBattingOrder(Integer battingOrder) {
        this.battingOrder = battingOrder;
    }

    public String getPitcher() {
        return pitcher;
    }

    public void setPitcher(String pitcher) {
        this.pitcher = pitcher;
    }

    public String getPitcherHand() {
        return pitcherHand;
    }

    public void setPitcherHand(String pitcherHand) {
        this.pitcherHand = pitcherHand;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
