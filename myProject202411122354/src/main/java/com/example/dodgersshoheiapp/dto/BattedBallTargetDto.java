package com.example.dodgersshoheiapp.dto;

public class BattedBallTargetDto {

    private Long gamePk;

    private Integer paNumber;

    public BattedBallTargetDto() {
    }

    public BattedBallTargetDto(
            Long gamePk,
            Integer paNumber) {

        this.gamePk = gamePk;
        this.paNumber = paNumber;
    }

    public Long getGamePk() {
        return gamePk;
    }

    public void setGamePk(Long gamePk) {
        this.gamePk = gamePk;
    }

    public Integer getPaNumber() {
        return paNumber;
    }

    public void setPaNumber(Integer paNumber) {
        this.paNumber = paNumber;
    }
}