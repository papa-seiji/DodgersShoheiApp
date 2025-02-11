package com.example.dodgersshoheiapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mlb_yosou_datas")
@JsonIgnoreProperties(ignoreUnknown = true) // ✅ JSON に含まれていても無視
public class MlbYosouData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ 修正: GenerationType.IDENTITY
    private Long id;

    @JsonProperty("yosouType") // ✅ JSON のキー名を明示
    @Column(name = "yosou_type")
    private String yosouType;

    @JsonProperty("yosouValue")
    @Column(name = "yosou_value")
    private String yosouValue;

    @JsonProperty("votedBy")
    @Column(name = "voted_by")
    private String votedBy;

    @JsonProperty("createdAt")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ✅ Getter & Setter
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
