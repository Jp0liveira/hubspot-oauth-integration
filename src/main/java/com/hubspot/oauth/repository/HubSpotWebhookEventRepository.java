package com.hubspot.oauth.repository;

import com.hubspot.oauth.entity.HubSpotWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HubSpotWebhookEventRepository extends JpaRepository<HubSpotWebhookEvent, Long> {
}
