package com.hubspot.oauth.controller;

import com.hubspot.oauth.dto.AuthorizationUrlResponseDTO;
import com.hubspot.oauth.service.HubSpotAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hubspot")
public class HubSpotController {

    private final HubSpotAuthService hubSpotAuthService;

    public HubSpotController(HubSpotAuthService hubSpotAuthService) {
        this.hubSpotAuthService = hubSpotAuthService;
    }

    @GetMapping("/authorize")
    public ResponseEntity<AuthorizationUrlResponseDTO> getAuthorizationUrl() {
        try {
            AuthorizationUrlResponseDTO response = hubSpotAuthService.generateAuthorizationUrl();
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new AuthorizationUrlResponseDTO("Erro ao gerar URL de autorização: " + e.getMessage()));
        }
    }
}
