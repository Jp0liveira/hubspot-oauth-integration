package com.hubspot.oauth.converter;

import com.hubspot.oauth.dto.HubSpotTokenDTO;
import com.hubspot.oauth.entity.HubSpotToken;
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

}
