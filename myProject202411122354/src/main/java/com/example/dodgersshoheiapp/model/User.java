package com.example.dodgersshoheiapp.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class) // 必須: JPA の監査リスナー
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主キー

    @Column(nullable = false, unique = true)
    private String username; // ユーザー名

    @Column(nullable = false)
    private String password; // パスワード

    @Column(nullable = false, length = 20) // ロールカラムのNULLを禁止し、文字列長制限
    private String role = "USER"; // ロール (デフォルト: USER)

    @CreatedDate
    @Column(updatable = false, nullable = false) // NULL値を防止し、更新を禁止
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private String icon; // アイコンのパス

    // コンストラクタ
    public User() {
    }

    // Getter and Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    // UserDetailsインターフェースの実装
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // 権限リスト（今回は空）
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // アカウントが有効期限切れではない
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // アカウントがロックされていない
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 資格情報が有効期限切れではない
    }

    @Override
    public boolean isEnabled() {
        return true; // ユーザーが有効
    }
}
