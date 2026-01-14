package com.example.dodgersshoheiapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "wbc_teams")
public class WbcTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int year;

    private String pool;

    @Column(name = "team_name")
    private String teamName;

    // ===== Getter / Setter =====

    public Long getId() {
        return id;
    }

    public int getYear() {
        return year;
    }

    public String getPool() {
        return pool;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setPool(String pool) {
        this.pool = pool;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
}
