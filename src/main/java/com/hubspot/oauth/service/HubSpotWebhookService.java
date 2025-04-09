package com.hubspot.oauth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.oauth.converter.HubSpotConverter;
import com.hubspot.oauth.dto.HubSpotWebhookEventDTO;
import com.hubspot.oauth.repository.HubSpotWebhookEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class HubSpotWebhookService {

    private final HubSpotWebhookEventRepository hubSpotWebhookEventRepository;

    public HubSpotWebhookService(HubSpotWebhookEventRepository hubSpotWebhookEventRepository) {
        this.hubSpotWebhookEventRepository = hubSpotWebhookEventRepository;
    }

    @Transactional
    public void processWebhookEvents(String requestBody) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        List<HubSpotWebhookEventDTO> events = Arrays.asList(
                mapper.readValue(requestBody, HubSpotWebhookEventDTO[].class)
        );

        events.stream()
                .filter(event -> "contact.creation".equalsIgnoreCase(event.getSubscriptionType()))
                .forEach(this::saveEvent);
    }

    public void saveEvent(HubSpotWebhookEventDTO hubSpotWebhookEventDTO) {
        hubSpotWebhookEventRepository.save(HubSpotConverter.converter(hubSpotWebhookEventDTO));
    }

}
