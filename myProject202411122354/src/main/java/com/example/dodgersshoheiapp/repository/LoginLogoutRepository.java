package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.LoginLogoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginLogoutRepository extends JpaRepository<LoginLogoutLog, Long> {
}
