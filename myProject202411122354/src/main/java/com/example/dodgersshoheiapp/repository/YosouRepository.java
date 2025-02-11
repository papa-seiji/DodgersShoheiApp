package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.MlbYosouData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface YosouRepository extends JpaRepository<MlbYosouData, Long> {

    // ✅ 予想タイプ & 予想値で検索（1人1票ルール用）
    Optional<MlbYosouData> findByYosouTypeAndYosouValue(String yosouType, String yosouValue);

    // ✅ 修正: 明示的な JPQL クエリを追加
    @Query("SELECT y FROM MlbYosouData y WHERE y.yosouType = :yosouType")
    List<MlbYosouData> findByYosouType(@Param("yosouType") String yosouType);
}
