package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.model.MlbYosouData;
import com.example.dodgersshoheiapp.repository.YosouRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class YosouService {
    private final YosouRepository yosouRepository;

    public YosouService(YosouRepository yosouRepository) {
        this.yosouRepository = yosouRepository;
    }

    // ✅ 投票データを登録（1人1票ルール）
    public void saveVote(MlbYosouData voteData) {
        Optional<MlbYosouData> existing = yosouRepository.findByYosouTypeAndYosouValue(voteData.getYosouType(),
                voteData.getYosouValue());

        if (existing.isPresent()) {
            MlbYosouData data = existing.get();
            if (!data.getVotedBy().contains(voteData.getVotedBy())) {
                data.setVotedBy(data.getVotedBy() + "," + voteData.getVotedBy());
                yosouRepository.save(data);
            }
        } else {
            yosouRepository.save(voteData);
        }
    }

    // ✅ 予想データを取得
    public List<MlbYosouData> getYosouByType(String yosouType) {
        return yosouRepository.findByYosouType(yosouType);
    }
}
