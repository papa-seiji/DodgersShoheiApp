package com.example.dodgersshoheiapp.dto;

public class CellDto {

    private boolean self;
    private boolean finished;
    private Integer homeScore;
    private Integer awayScore;
    private Boolean win;

    /* ===== 表示用 ===== */
    public String getDisplay() {

        // 自分同士のセルだけ無効
        if (self && homeScore == null && awayScore == null) {
            return "—";
        }

        if (!finished || homeScore == null || awayScore == null) {
            return "—";
        }

        String mark = (win != null && win) ? "◯" : "●";
        return mark + "<br>" + homeScore + "-" + awayScore;
    }

    /* ===== getter / setter ===== */

    public boolean isSelf() {
        return self;
    }

    public void setSelf(boolean self) {
        this.self = self;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public Integer getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(Integer homeScore) {
        this.homeScore = homeScore;
    }

    public Integer getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(Integer awayScore) {
        this.awayScore = awayScore;
    }

    public Boolean getWin() {
        return win;
    }

    public void setWin(Boolean win) {
        this.win = win;
    }
}
