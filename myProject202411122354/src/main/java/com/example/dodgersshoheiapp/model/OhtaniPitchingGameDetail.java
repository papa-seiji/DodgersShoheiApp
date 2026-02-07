package com.example.dodgersshoheiapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ohtani_pitching_game_details")
public class OhtaniPitchingGameDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    // inning01 ～ inning10（今回は文字列でOK）
    private String inning01;
    private String inning02;
    private String inning03;
    private String inning04;
    private String inning05;
    private String inning06;
    private String inning07;
    private String inning08;
    private String inning09;
    private String inning10;

    /* ===== getter / setter ===== */

    public Long getId() {
        return id;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getInning01() {
        return inning01;
    }

    public void setInning01(String inning01) {
        this.inning01 = inning01;
    }

    public String getInning02() {
        return inning02;
    }

    public void setInning02(String inning02) {
        this.inning02 = inning02;
    }

    public String getInning03() {
        return inning03;
    }

    public void setInning03(String inning03) {
        this.inning03 = inning03;
    }

    public String getInning04() {
        return inning04;
    }

    public void setInning04(String inning04) {
        this.inning04 = inning04;
    }

    public String getInning05() {
        return inning05;
    }

    public void setInning05(String inning05) {
        this.inning05 = inning05;
    }

    public String getInning06() {
        return inning06;
    }

    public void setInning06(String inning06) {
        this.inning06 = inning06;
    }

    public String getInning07() {
        return inning07;
    }

    public void setInning07(String inning07) {
        this.inning07 = inning07;
    }

    public String getInning08() {
        return inning08;
    }

    public void setInning08(String inning08) {
        this.inning08 = inning08;
    }

    public String getInning09() {
        return inning09;
    }

    public void setInning09(String inning09) {
        this.inning09 = inning09;
    }

    public String getInning10() {
        return inning10;
    }

    public void setInning10(String inning10) {
        this.inning10 = inning10;
    }
}
