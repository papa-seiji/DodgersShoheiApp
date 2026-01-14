// package com.example.dodgersshoheiapp.service;

// public class WbcPoolMatchService {

// }

package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.model.WbcPoolMatch;
import com.example.dodgersshoheiapp.repository.WbcPoolMatchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WbcPoolMatchService {

    private final WbcPoolMatchRepository repository;

    public WbcPoolMatchService(WbcPoolMatchRepository repository) {
        this.repository = repository;
    }

    /** 🌍 全体表示（年度指定） */
    public List<WbcPoolMatch> getAllMatchesByYear(Integer year) {
        return repository.findByYear(year);
    }

    /** POOL指定（後続ステップ用） */
    public List<WbcPoolMatch> getMatchesByYearAndPool(Integer year, String pool) {
        return repository.findByYearAndPool(year, pool);
    }
}
