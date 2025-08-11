package com.example.dodgersshoheiapp.dto;

import java.util.List;

public class TeamLineupDto {
    private String teamName;
    private PitcherDto probablePitcher;
    private List<BatterDto> lineup;

    public TeamLineupDto() {
    }

    public TeamLineupDto(String teamName, PitcherDto probablePitcher, List<BatterDto> lineup) {
        this.teamName = teamName;
        this.probablePitcher = probablePitcher;
        this.lineup = lineup;
    }

    public String getTeamName() {
        return teamName;
    }

    public PitcherDto getProbablePitcher() {
        return probablePitcher;
    }

    public List<BatterDto> getLineup() {
        return lineup;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public void setProbablePitcher(PitcherDto probablePitcher) {
        this.probablePitcher = probablePitcher;
    }

    public void setLineup(List<BatterDto> lineup) {
        this.lineup = lineup;
    }
}
