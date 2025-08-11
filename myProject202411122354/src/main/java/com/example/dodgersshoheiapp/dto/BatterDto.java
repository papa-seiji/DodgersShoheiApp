package com.example.dodgersshoheiapp.dto;

public class BatterDto implements Comparable<BatterDto> {
    private long id;
    private String name;
    private String pos; // 例: "C", "1B", "RF"
    private int order; // 1〜9
    private boolean japanese;

    public BatterDto() {
    }

    public BatterDto(long id, String name, String pos, int order, boolean japanese) {
        this.id = id;
        this.name = name;
        this.pos = pos;
        this.order = order;
        this.japanese = japanese;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPos() {
        return pos;
    }

    public int getOrder() {
        return order;
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

    public void setPos(String pos) {
        this.pos = pos;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setJapanese(boolean japanese) {
        this.japanese = japanese;
    }

    @Override
    public int compareTo(BatterDto o) {
        return Integer.compare(this.order, o.order);
    }
}
