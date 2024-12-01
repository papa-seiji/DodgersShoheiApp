package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.ProudComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProudCommentRepository extends JpaRepository<ProudComment, Long> {
    List<ProudComment> findByImageId(Long imageId);
}
