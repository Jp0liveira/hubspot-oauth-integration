package com.hubspot.oauth.dto;

public class AuthorizationUrlResponseDTO {
    private String authorizationUrl;

  public AuthorizationUrlResponseDTO(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

}
