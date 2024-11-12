package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}