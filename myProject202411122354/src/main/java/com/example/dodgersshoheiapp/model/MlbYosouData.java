package com.example.dodgersshoheiapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mlb_yosou_datas")
public class MlbYosouData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String yosouType;

    @Column(nullable = false)
    private String yosouValue;

    @Column(nullable = false)
    private String votedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ✅ Getter メソッドを追加（Lombok なし）
    public Long getId() {
        return id;
    }

    public String getYosouType() {
        return yosouType;
    }

    public String getYosouValue() {
        return yosouValue;
    }

    public String getVotedBy() {
        return votedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ✅ Setter メソッド（必要なら追加）
    public void setYosouType(String yosouType) {
        this.yosouType = yosouType;
    }

    public void setYosouValue(String yosouValue) {
        this.yosouValue = yosouValue;
    }

    public void setVotedBy(String votedBy) {
        this.votedBy = votedBy;
    }
}
