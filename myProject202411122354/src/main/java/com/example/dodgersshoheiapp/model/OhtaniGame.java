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
    private String comment;

    // 🔥 MLB API 連携用（gamePk）
    private Long gamePk;

    // 🔥 打席詳細
    private List<OhtaniGameDetail> details;

    // 🔥 Linescore（APIから取得・DB保存しない）
    private List<Integer> homeRunsByInning;
    private List<Integer> awayRunsByInning;

    // ★★★ 追加：合計得点（Controller計算用） ★★★
    private Integer homeTotalRuns;
    private Integer awayTotalRuns;

    // 🔥 H / E 追加
    private Integer homeHits;
    private Integer awayHits;
    private Integer homeErrors;
    private Integer awayErrors;

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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    // ===== gamePk =====

    public Long getGamePk() {
        return gamePk;
    }

    public void setGamePk(Long gamePk) {
        this.gamePk = gamePk;
    }

    // ===== details =====

    public List<OhtaniGameDetail> getDetails() {
        return details;
    }

    public void setDetails(List<OhtaniGameDetail> details) {
        this.details = details;
    }

    // ===== Linescore =====

    public List<Integer> getHomeRunsByInning() {
        return homeRunsByInning;
    }

    public void setHomeRunsByInning(List<Integer> homeRunsByInning) {
        this.homeRunsByInning = homeRunsByInning;
    }

    public List<Integer> getAwayRunsByInning() {
        return awayRunsByInning;
    }

    public void setAwayRunsByInning(List<Integer> awayRunsByInning) {
        this.awayRunsByInning = awayRunsByInning;
    }

    // ===== ★ 合計得点 =====

    public Integer getHomeTotalRuns() {
        return homeTotalRuns;
    }

    public void setHomeTotalRuns(Integer homeTotalRuns) {
        this.homeTotalRuns = homeTotalRuns;
    }

    public Integer getAwayTotalRuns() {
        return awayTotalRuns;
    }

    public void setAwayTotalRuns(Integer awayTotalRuns) {
        this.awayTotalRuns = awayTotalRuns;
    }

    // ===== H / E =====

    public Integer getHomeHits() {
        return homeHits;
    }

    public void setHomeHits(Integer homeHits) {
        this.homeHits = homeHits;
    }

    public Integer getAwayHits() {
        return awayHits;
    }

    public void setAwayHits(Integer awayHits) {
        this.awayHits = awayHits;
    }

    public Integer getHomeErrors() {
        return homeErrors;
    }

    public void setHomeErrors(Integer homeErrors) {
        this.homeErrors = homeErrors;
    }

    public Integer getAwayErrors() {
        return awayErrors;
    }

    public void setAwayErrors(Integer awayErrors) {
        this.awayErrors = awayErrors;
    }

}
