package com.hubspot.oauth.service;

import com.hubspot.oauth.dto.AuthorizationUrlResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class HubSpotAuthService {

    @Value("${hubspot.client-id}")
    private String clientId;

    @Value("${hubspot.redirect-uri}")
    private String redirectUri;

    @Value("${hubspot.scopes}")
    private String scopes;

    @Value("${hubspot.authorize-url}")
    private String authorizeUrl;

    public AuthorizationUrlResponseDTO generateAuthorizationUrl() {
        String url = UriComponentsBuilder
                .fromUriString(authorizeUrl)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", scopes)
                .queryParam("response_type", "code")
                .toUriString();

        System.out.println(url);
        return new AuthorizationUrlResponseDTO(url);
    }

}
