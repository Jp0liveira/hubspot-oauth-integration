package com.hubspot.oauth.repository;

import com.hubspot.oauth.entity.HubSpotToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HubSpotTokenRepository extends JpaRepository<HubSpotToken, Long> {
    Optional<HubSpotToken> findTopByOrderByIssuedAtDesc();

}
