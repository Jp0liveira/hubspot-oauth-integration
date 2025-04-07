package com.hubspot.oauth.dto;

import lombok.Data;

@Data
public class AuthorizationUrlResponseDTO {
    private String authorizationUrl;

  public AuthorizationUrlResponseDTO(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

}
