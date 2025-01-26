package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.NewsUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsUpdateRepository extends JpaRepository<NewsUpdate, Long> {
    List<NewsUpdate> findAllByOrderByCreatedAtDesc();
}
