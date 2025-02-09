package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.MlbYosouData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface YosouRepository extends JpaRepository<MlbYosouData, Long> {
    // ✅ 予想タイプ & 予想値で検索（1人1票の判定に必要）
    Optional<MlbYosouData> findByYosouTypeAndYosouValue(String yosouType, String yosouValue);

    // ✅ 予想タイプでリスト取得
    List<MlbYosouData> findByYosouType(String yosouType);
}
