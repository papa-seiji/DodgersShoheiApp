package com.example.dodgersshoheiapp.dto;

public class OpsTrendDto {

    private String gameDate;
    private Double cumulativeOps;

    public OpsTrendDto() {
    }

    public OpsTrendDto(String gameDate, Double cumulativeOps) {
        this.gameDate = gameDate;
        this.cumulativeOps = cumulativeOps;
    }

    public String getGameDate() {
        return gameDate;
    }

    public void setGameDate(String gameDate) {
        this.gameDate = gameDate;
    }

    public Double getCumulativeOps() {
        return cumulativeOps;
    }

    public void setCumulativeOps(Double cumulativeOps) {
        this.cumulativeOps = cumulativeOps;
    }
}