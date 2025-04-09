package com.hubspot.oauth.converter;

import com.hubspot.oauth.dto.HubSpotTokenDTO;
import com.hubspot.oauth.dto.HubSpotWebhookEventDTO;
import com.hubspot.oauth.entity.HubSpotToken;
import com.hubspot.oauth.entity.HubSpotWebhookEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class HubSpotConverter {
    HubSpotConverter(){};

    public static HubSpotToken converter(HubSpotTokenDTO hubSpotTokenDTO){
        HubSpotToken  hubSpotToken = new HubSpotToken();
        hubSpotToken.setId(hubSpotTokenDTO.id());
        hubSpotToken.setTokenType(hubSpotTokenDTO.tokenType());
        hubSpotToken.setAccessToken(hubSpotTokenDTO.accessToken());
        hubSpotToken.setRefreshToken(hubSpotTokenDTO.refreshToken());
        hubSpotToken.setExpiresIn(hubSpotTokenDTO.expiresIn());
        hubSpotToken.setIssuedAt(LocalDateTime.now());
        return hubSpotToken;
    }

    public static HubSpotWebhookEvent converter(HubSpotWebhookEventDTO hubSpotWebhookEventDTO) {
        HubSpotWebhookEvent hubSpotWebhookEvent = new HubSpotWebhookEvent();
        hubSpotWebhookEvent.setEventId(hubSpotWebhookEventDTO.getEventId());
        hubSpotWebhookEvent.setSubscriptionId(hubSpotWebhookEventDTO.getSubscriptionId());
        hubSpotWebhookEvent.setPortalId(hubSpotWebhookEventDTO.getPortalId());
        hubSpotWebhookEvent.setAppId(hubSpotWebhookEventDTO.getAppId());
        hubSpotWebhookEvent.setOccurredAt(hubSpotWebhookEventDTO.getOccurredAt());
        hubSpotWebhookEvent.setSubscriptionType(hubSpotWebhookEventDTO.getSubscriptionType());
        hubSpotWebhookEvent.setAttemptNumber(hubSpotWebhookEventDTO.getAttemptNumber());
        hubSpotWebhookEvent.setObjectId(hubSpotWebhookEventDTO.getObjectId());
        hubSpotWebhookEvent.setChangeFlag(hubSpotWebhookEventDTO.getChangeFlag());
        hubSpotWebhookEvent.setChangeSource(hubSpotWebhookEventDTO.getChangeSource());
        hubSpotWebhookEvent.setSourceId(hubSpotWebhookEventDTO.getSourceId());
        return hubSpotWebhookEvent;
    }

}
