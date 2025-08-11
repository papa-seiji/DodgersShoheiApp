package com.example.dodgersshoheiapp.dto;

public class PitcherDto {
    private long id;
    private String name;
    private boolean japanese;

    public PitcherDto() {
    }

    public PitcherDto(long id, String name, boolean japanese) {
        this.id = id;
        this.name = name;
        this.japanese = japanese;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isJapanese() {
        return japanese;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setJapanese(boolean japanese) {
        this.japanese = japanese;
    }
}
