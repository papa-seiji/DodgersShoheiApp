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
    private String yosouType; // 予想の種類 (例: NL_WEST_1位)

    @Column(nullable = false)
    private String yosouValue; // 予想内容 (例: ドジャース)

    @Column(nullable = false)
    private String votedBy; // 投票したユーザー

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 作成日時

    // ✅ Getter メソッド
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

    // ✅ Setter メソッド
    public void setId(Long id) {
        this.id = id;
    }

    public void setYosouType(String yosouType) {
        this.yosouType = yosouType;
    }

    public void setYosouValue(String yosouValue) {
        this.yosouValue = yosouValue;
    }

    public void setVotedBy(String votedBy) {
        this.votedBy = votedBy;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
