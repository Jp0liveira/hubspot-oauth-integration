package com.hubspot.oauth.controller;

import com.hubspot.oauth.dto.AuthorizationUrlResponseDTO;
import com.hubspot.oauth.dto.ContactRequestDTO;
import com.hubspot.oauth.dto.ContactResponseDTO;
import com.hubspot.oauth.dto.OAuthCallbackResponseDTO;
import com.hubspot.oauth.service.HubSpotAuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/hubspot")
public class HubSpotController {

    private final HubSpotAuthService hubSpotAuthService;

    public HubSpotController(HubSpotAuthService hubSpotAuthService) {
        this.hubSpotAuthService = hubSpotAuthService;
    }

    @GetMapping("/authorize")
    public ResponseEntity<Void> getAuthorizationUrl() {
        try {
            AuthorizationUrlResponseDTO response = hubSpotAuthService.generateAuthorizationUrl();
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(response.getAuthorizationUrl()));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } catch (Exception e) {
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create("/error?message=" + URLEncoder.encode("Erro ao gerar URL: " + e.getMessage(), StandardCharsets.UTF_8)));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
    }

    @GetMapping("/oauth/callback")
    public ResponseEntity<OAuthCallbackResponseDTO> handleOAuthCallback(@RequestParam("code") String code) {
        try {
            OAuthCallbackResponseDTO response = hubSpotAuthService.exchangeCodeForToken(code);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new OAuthCallbackResponseDTO(null, "Erro: " + e.getMessage()));
        }
    }

    @PostMapping("/contacts")
    public ResponseEntity<ContactResponseDTO> createContact(@Validated @RequestBody ContactRequestDTO contactRequest) {
        try {
            ContactResponseDTO response = hubSpotAuthService.createContact(contactRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(new ContactResponseDTO("Erro: " + e.getMessage(), null));
        }
    }

}
