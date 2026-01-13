package com.example.dodgersshoheiapp.dto;

public class TeamStatDto {

    private int win;
    private int lose;
    private int runDiff;
    private int rank; // ★ 追加

    public int getWin() {
        return win;
    }

    public void addWin() {
        this.win++;
    }

    public int getLose() {
        return lose;
    }

    public void addLose() {
        this.lose++;
    }

    public int getRunDiff() {
        return runDiff;
    }

    public void addRunDiff(int diff) {
        this.runDiff += diff;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
