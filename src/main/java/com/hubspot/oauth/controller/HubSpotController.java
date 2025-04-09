package com.hubspot.oauth.controller;

import com.hubspot.oauth.dto.*;
import com.hubspot.oauth.service.HubSpotAuthService;
import com.hubspot.oauth.service.HubSpotWebhookService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/hubspot")
public class HubSpotController {

    private final HubSpotAuthService hubSpotAuthService;
    private final HubSpotWebhookService hubSpotWebhookService;

    public HubSpotController(HubSpotAuthService hubSpotAuthService, HubSpotWebhookService hubSpotWebhookService) {
        this.hubSpotAuthService = hubSpotAuthService;
        this.hubSpotWebhookService = hubSpotWebhookService;
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ContactResponseDTO("Erro: " + e.getMessage(), null));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody(required = false) List<HubSpotWebhookEventDTO> events,
            @RequestHeader(value = "X-HubSpot-Signature", required = false) String signature,
            HttpServletRequest request) {
        try {
            String rawBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (hubSpotWebhookService.validateSignature(signature, rawBody)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            hubSpotWebhookService.processWebhookEvents(events);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
