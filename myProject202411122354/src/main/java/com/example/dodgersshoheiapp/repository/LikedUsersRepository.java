package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.LikedUsers;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikedUsersRepository extends JpaRepository<LikedUsers, Long> {
}
