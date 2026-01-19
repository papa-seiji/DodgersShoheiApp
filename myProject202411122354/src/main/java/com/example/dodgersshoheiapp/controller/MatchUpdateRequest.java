package com.example.dodgersshoheiapp.controller;

public class MatchUpdateRequest {

    private int year;
    private String round;
    private int matchNo;
    private String homeTeam;
    private Integer homeScore;
    private String awayTeam;
    private Integer awayScore;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getRound() {
        return round;
    }

    public void setRound(String round) {
        this.round = round;
    }

    public int getMatchNo() {
        return matchNo;
    }

    public void setMatchNo(int matchNo) {
        this.matchNo = matchNo;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Integer getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(Integer homeScore) {
        this.homeScore = homeScore;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public Integer getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(Integer awayScore) {
        this.awayScore = awayScore;
    }
}
