package com.example.dodgersshoheiapp.dto;

public class HitDirectionStatsDto {

    // ============================================
    // ★ 件数
    // ============================================

    private int pullCount;

    private int centerCount;

    private int oppositeCount;

    // ============================================
    // ★ 割合
    // ============================================

    private double pullPercent;

    private double centerPercent;

    private double oppositePercent;

    // ============================================
    // ★ 合計
    // ============================================

    private int total;

    // ============================================
    // ★ Getter / Setter
    // ============================================

    public int getPullCount() {
        return pullCount;
    }

    public void setPullCount(int pullCount) {
        this.pullCount = pullCount;
    }

    public int getCenterCount() {
        return centerCount;
    }

    public void setCenterCount(int centerCount) {
        this.centerCount = centerCount;
    }

    public int getOppositeCount() {
        return oppositeCount;
    }

    public void setOppositeCount(int oppositeCount) {
        this.oppositeCount = oppositeCount;
    }

    public double getPullPercent() {
        return pullPercent;
    }

    public void setPullPercent(double pullPercent) {
        this.pullPercent = pullPercent;
    }

    public double getCenterPercent() {
        return centerPercent;
    }

    public void setCenterPercent(double centerPercent) {
        this.centerPercent = centerPercent;
    }

    public double getOppositePercent() {
        return oppositePercent;
    }

    public void setOppositePercent(double oppositePercent) {
        this.oppositePercent = oppositePercent;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}