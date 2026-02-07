package com.example.dodgersshoheiapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "ohtani_pitching_games")
public class OhtaniPitchingGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_date", nullable = false)
    private LocalDate gameDate;

    @Column(name = "opponent")
    private String opponent;

    @Column(name = "result")
    private String result;

    @Column(name = "win_lose")
    private String winLose;

    @Column(name = "total_pitches")
    private Integer totalPitches;

    // ★ 追加①：評価（S/A/B/C/D）
    @Column(name = "form_value")
    private String formValue;

    // ★ 追加②：総評コメント
    @Column(name = "comment")
    private String comment;

    /* ===== getter / setter ===== */

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

    public String getWinLose() {
        return winLose;
    }

    public void setWinLose(String winLose) {
        this.winLose = winLose;
    }

    public Integer getTotalPitches() {
        return totalPitches;
    }

    public void setTotalPitches(Integer totalPitches) {
        this.totalPitches = totalPitches;
    }

    // ★ 追加 getter / setter
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
}
