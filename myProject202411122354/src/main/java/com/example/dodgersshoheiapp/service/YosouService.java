package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.model.MlbYosouData;
import com.example.dodgersshoheiapp.repository.YosouRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class YosouService {
    private final YosouRepository yosouRepository;

    public YosouService(YosouRepository yosouRepository) {
        this.yosouRepository = yosouRepository;
    }

    public void saveVote(MlbYosouData voteData) {
        System.out.println("📝 Service: 投票データ保存開始: " + voteData);
        yosouRepository.save(voteData);
        System.out.println("✅ Service: 投票データ保存完了");
    }

    public List<MlbYosouData> getYosouByType(String yosouType) {
        System.out.println("🔍 Service: 予想データ取得処理開始: " + yosouType);
        List<MlbYosouData> result = yosouRepository.findByYosouType(yosouType);

        System.out.println("✅ Service: 取得成功: " + result);
        return result;
    }
}
