package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.model.MlbYosouData;
import com.example.dodgersshoheiapp.repository.YosouRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class YosouService {
    private final YosouRepository yosouRepository;

    public YosouService(YosouRepository yosouRepository) {
        this.yosouRepository = yosouRepository;
    }

    @Transactional
    public void saveVote(MlbYosouData voteData) {
        System.out.println("📝 Service: 投票データ保存開始: " + voteData);

        // ✅ 既存の投票データがあるか確認（最新1件のみ取得）
        Optional<MlbYosouData> existingVote = yosouRepository.findTopByYosouTypeAndVotedByOrderByCreatedAtDesc(
                voteData.getYosouType(), voteData.getVotedBy());

        if (existingVote.isPresent()) {
            // ✅ 既存データがあれば更新
            MlbYosouData existing = existingVote.get();
            existing.setYosouValue(voteData.getYosouValue());
            yosouRepository.save(existing);
            System.out.println("🔄 Service: 投票データ更新完了");
        } else {
            // ✅ 既存データがなければ新規登録
            yosouRepository.save(voteData);
            System.out.println("✅ Service: 投票データ新規登録完了");
        }
    }

    public List<MlbYosouData> getYosouByType(String yosouType) {
        return yosouRepository.findByYosouType(yosouType);
    }

    public Optional<MlbYosouData> getUserVote(String yosouType, String votedBy) {
        return yosouRepository.findTopByYosouTypeAndVotedByOrderByCreatedAtDesc(yosouType, votedBy);
    }
}
