package com.example.dodgersshoheiapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.dodgersshoheiapp.model.NewsUpdate;

@Repository
public interface NewsUpdateRepository extends JpaRepository<NewsUpdate, Long> {
    List<NewsUpdate> findAllByOrderByCreatedAtDesc();
}
