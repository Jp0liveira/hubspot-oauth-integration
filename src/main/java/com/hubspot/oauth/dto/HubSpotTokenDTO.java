package com.hubspot.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record HubSpotTokenDTO(Long id,
                              @JsonProperty("token_type")
                              String tokenType,
                              @JsonProperty("access_token")
                              String accessToken,
                              @JsonProperty("refresh_token")
                              String refreshToken,
                              @JsonProperty("expires_in")
                              Long expiresIn,
                              LocalDateTime issuedAt) {
}
