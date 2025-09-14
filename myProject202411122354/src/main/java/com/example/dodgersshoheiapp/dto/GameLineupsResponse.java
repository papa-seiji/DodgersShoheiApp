package com.example.dodgersshoheiapp.dto;

public class GameLineupsResponse {
    private TeamLineupDto home;
    private TeamLineupDto away;

    public GameLineupsResponse() {
    }

    public GameLineupsResponse(TeamLineupDto home, TeamLineupDto away) {
        this.home = home;
        this.away = away;
    }

    public TeamLineupDto getHome() {
        return home;
    }

    public TeamLineupDto getAway() {
        return away;
    }

    public void setHome(TeamLineupDto home) {
        this.home = home;
    }

    public void setAway(TeamLineupDto away) {
        this.away = away;
    }
}
