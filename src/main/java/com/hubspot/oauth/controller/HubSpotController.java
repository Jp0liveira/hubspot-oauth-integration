package com.hubspot.oauth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.oauth.dto.*;
import com.hubspot.oauth.exception.InvalidSignatureException;
import com.hubspot.oauth.service.HubSpotAuthService;
import com.hubspot.oauth.service.HubSpotWebhookService;
import com.hubspot.oauth.service.HubSpotWebhookValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/hubspot")
public class HubSpotController {

    private static final Logger logger = LoggerFactory.getLogger(HubSpotController.class);

    private final HubSpotAuthService hubSpotAuthService;
    private final HubSpotWebhookService hubSpotWebhookService;
    private final HubSpotWebhookValidator validator;


    public HubSpotController(HubSpotAuthService hubSpotAuthService, HubSpotWebhookService hubSpotWebhookService, HubSpotWebhookValidator validator) {
        this.hubSpotAuthService = hubSpotAuthService;
        this.hubSpotWebhookService = hubSpotWebhookService;
        this.validator = validator;
    }

    @GetMapping("/authorize")
    public ResponseEntity<Void> getAuthorizationUrl() {
        try {
            AuthorizationUrlResponseDTO response = hubSpotAuthService.generateAuthorizationUrl();
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(response.getAuthorizationUrl()))
                    .build() ;
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
            @RequestHeader("X-HubSpot-Signature") String signature,
            @RequestHeader(value = "X-HubSpot-Signature-Version", defaultValue = "v1") String signatureVersion,
            @RequestBody(required = false) String requestBody) {
        try {

            boolean isValid = validator.validateWebhook(
                    signature,
                    signatureVersion,
                    requestBody);

            if (!isValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            hubSpotWebhookService.processWebhookEvents(requestBody);
            return ResponseEntity.ok().build();
        } catch (InvalidSignatureException e) {
            logger.error("Falha na validação do webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            logger.error("Erro ao processar webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
