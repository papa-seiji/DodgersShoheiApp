package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.MlbYosouData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface YosouRepository extends JpaRepository<MlbYosouData, Long> {

    // ✅ 指定した `yosouType` に属するデータをすべて取得
    List<MlbYosouData> findByYosouType(String yosouType);

    // ✅ 特定の `yosouType` で、指定した `votedBy`（ユーザー）の投票データを取得
    Optional<MlbYosouData> findTopByYosouTypeAndVotedByOrderByCreatedAtDesc(String yosouType, String votedBy);
}
