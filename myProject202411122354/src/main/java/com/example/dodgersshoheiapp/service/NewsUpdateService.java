package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.model.NewsUpdate;
import com.example.dodgersshoheiapp.repository.NewsUpdateRepository;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsUpdateService {

    @Autowired
    private NewsUpdateRepository newsUpdateRepository;

    // データベースからデータを取得し、エスケープ処理を適用
    public List<NewsUpdate> getAllNewsUpdates() {
        List<NewsUpdate> rawNewsUpdates = newsUpdateRepository.findAllByOrderByCreatedAtDesc();
        return escapeNewsDetails(rawNewsUpdates);
    }

    // HTMLエスケープ処理を行う
    public List<NewsUpdate> escapeNewsDetails(List<NewsUpdate> newsUpdates) {
        return newsUpdates.stream().map(news -> {
            if (news.getDetails() != null) {
                news.setDetails(StringEscapeUtils.escapeHtml4(news.getDetails()));
            }
            if (news.getImageUrl() != null) {
                news.setImageUrl(StringEscapeUtils.escapeHtml4(news.getImageUrl()));
            }
            return news;
        }).collect(Collectors.toList());
    }
}
