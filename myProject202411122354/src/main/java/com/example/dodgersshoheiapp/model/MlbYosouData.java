package com.example.dodgersshoheiapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "mlb_yosou_datas")
@Getter
@Setter
public class MlbYosouData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String yosouType; // 予想の種類 (例: NL_WEST_1位)
    private String yosouValue; // 予想内容 (例: ドジャース)
    private String votedBy; // 投票したユーザー
    private LocalDateTime createdAt = LocalDateTime.now(); // 作成日時
}
