package com.example.dodgersshoheiapp.service;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;

import com.example.dodgersshoheiapp.model.NewsUpdate;
import com.example.dodgersshoheiapp.repository.NewsUpdateRepository;
import org.apache.commons.text.StringEscapeUtils;
import java.util.List;

@Service
public class NewsUpdateService {

    private final NewsUpdateRepository newsUpdateRepository;

    public NewsUpdateService(NewsUpdateRepository newsUpdateRepository) {
        this.newsUpdateRepository = newsUpdateRepository;
    }

    public List<NewsUpdate> getAllNewsUpdates() {
        List<NewsUpdate> updates = newsUpdateRepository.findAllByOrderByCreatedAtDesc();
        updates.forEach(update -> {
            update.setContent(StringEscapeUtils.escapeHtml4(update.getContent()));
            if (update.getDetails() != null) {
                update.setDetails(StringEscapeUtils.escapeHtml4(update.getDetails()));
            }
            if (update.getImageUrl() != null) {
                update.setImageUrl(StringEscapeUtils.escapeHtml4(update.getImageUrl()));
            }
        });
        return updates;
    }
}
