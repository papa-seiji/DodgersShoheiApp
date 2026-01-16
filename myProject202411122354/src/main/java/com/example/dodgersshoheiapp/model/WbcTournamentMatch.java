package com.example.dodgersshoheiapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "wbc_tournament_matches", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "year", "round", "match_no" })
})
public class WbcTournamentMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int year;

    private String round;

    @Column(name = "match_no")
    private int matchNo;

    private String homeTeam;
    private String awayTeam;

    private Integer homeScore;
    private Integer awayScore;

    private String winnerTeam;

    private LocalDate matchDate;
    private LocalTime matchTime;
    private String stadium;

    /* ✅ JPA 必須（これが無かったのが今回の地雷） */
    public WbcTournamentMatch() {
    }

    /* ---- getter / setter ---- */

    public Long getId() {
        return id;
    }

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

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public String getWinnerTeam() {
        return winnerTeam;
    }

    public void setWinnerTeam(String winnerTeam) {
        this.winnerTeam = winnerTeam;
    }
}
