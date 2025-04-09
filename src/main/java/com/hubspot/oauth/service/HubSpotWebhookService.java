package com.hubspot.oauth.service;

import com.hubspot.oauth.dto.HubSpotWebhookEventDTO;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HubSpotWebhookService {
    private static final Logger logger = LoggerFactory.getLogger(HubSpotWebhookService.class);

    @Value("${hubspot.client-secret}")
    private String clientSecret;


    public void processWebhookEvents(List<HubSpotWebhookEventDTO> events) {
        for (HubSpotWebhookEventDTO event : events) {
            if ("contact.creation".equals(event.getSubscriptionType())) {
                logger.info("Novo contato criado no HubSpot: ID={}",  event.getObjectId());
            } else {
                logger.debug("Evento ignorado: Tipo={}", event.getSubscriptionType());
            }
        }
    }

    public boolean validateSignature(String signature, String requestBody) {
        if (signature == null || requestBody == null) {
            return false;
        }
        String calculatedSignature = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, clientSecret).hmacHex(requestBody);
        return signature.equals(calculatedSignature);
    }

}
