package com.example.dodgersshoheiapp.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "liked_users")
public class LikedUsers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private ProudImage proudImage;

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

    public ProudImage getProudImage() {
        return proudImage;
    }

    public void setProudImage(ProudImage proudImage) {
        this.proudImage = proudImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LikedUsers that = (LikedUsers) o;
        return Objects.equals(username, that.username) &&
                Objects.equals(proudImage, that.proudImage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, proudImage);
    }
}
