package com.example.urlshortener.repository;

import com.example.urlshortener.entity.ShortUrl;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortCodeAndActiveTrue(String shortCode);

    boolean existsByShortCode(String shortCode);

    List<ShortUrl> findAllByOwnerId(Long ownerId);
}
