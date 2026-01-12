package com.example.dodgersshoheiapp.dto;

public class WbcPoolStandingDto {

    private Integer year;
    private String pool;
    private String team;

    private int wins = 0;
    private int losses = 0;
    private int scoredRuns = 0;
    private int allowedRuns = 0;

    private int rank;

    // ⭐ 通過フラグ（追加）
    private boolean qualified = false;

    public WbcPoolStandingDto(Integer year, String pool, String team) {
        this.year = year;
        this.pool = pool;
        this.team = team;
    }

    /*
     * =====================
     * 試合結果の集計
     * =====================
     */

    public void addScore(int scored, int allowed) {
        this.scoredRuns += scored;
        this.allowedRuns += allowed;
    }

    public void win() {
        this.wins++;
    }

    public void lose() {
        this.losses++;
    }

    /*
     * =====================
     * Getter
     * =====================
     */

    public Integer getYear() {
        return year;
    }

    public String getPool() {
        return pool;
    }

    public String getTeam() {
        return team;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getScoredRuns() {
        return scoredRuns;
    }

    public int getAllowedRuns() {
        return allowedRuns;
    }

    /** 得失点差 */
    public int getRunDiff() {
        return scoredRuns - allowedRuns;
    }

    public int getRank() {
        return rank;
    }

    public boolean isQualified() {
        return qualified;
    }

    /*
     * =====================
     * Setter
     * =====================
     */

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setQualified(boolean qualified) {
        this.qualified = qualified;
    }
}
