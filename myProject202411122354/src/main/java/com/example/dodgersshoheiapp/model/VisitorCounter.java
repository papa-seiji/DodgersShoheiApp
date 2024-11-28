package com.example.dodgersshoheiapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visitor_counter")
public class VisitorCounter {

    @Id
    private Integer id; // IDをInteger型に変更

    @Column(nullable = false)
    private int value; // 訪問者数

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 更新日時

    // デフォルトコンストラクタ（JPAで必要）
    public VisitorCounter() {
    }

    // カスタムコンストラクタ（idとvalueを受け取る）
    public VisitorCounter(Integer id, int value) {
        this.id = id;
        this.value = value;
        this.updatedAt = LocalDateTime.now(); // 初期化時に現在日時を設定
    }

    // GetterとSetter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // 更新時にupdatedAtを自動設定
    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
