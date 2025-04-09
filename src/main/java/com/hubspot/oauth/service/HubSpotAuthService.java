package com.hubspot.oauth.service;

import com.hubspot.oauth.dto.*;
import com.hubspot.oauth.entity.HubSpotToken;
import com.hubspot.oauth.repository.HubSpotTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class HubSpotAuthService {

    private static final Logger logger = LoggerFactory.getLogger(HubSpotAuthService.class);

    @Value("${hubspot.client-id}")
    private String clientId;

    @Value("${hubspot.client-secret}")
    private String clientSecret;

    @Value("${hubspot.redirect-uri}")
    private String redirectUri;

    @Value("${hubspot.scopes}")
    private String scopes;

    @Value("${hubspot.authorize-url}")
    private String authorizeUrl;

    @Value("${hubspot.token-url}")
    private String tokenUrl;

    @Value("${hubspot.contacts-url}")
    private String contactsUrl;

    private final HubSpotTokenRepository tokenRepository;

    private final RestTemplate restTemplate;

    public HubSpotAuthService(HubSpotTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
        this.restTemplate = new RestTemplate();
    }

    public AuthorizationUrlResponseDTO generateAuthorizationUrl() {
        String url = UriComponentsBuilder
                .fromUriString(authorizeUrl)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", scopes)
                .toUriString();
        logger.info("Obtendo código de autorização: URL={}", url);
        return new AuthorizationUrlResponseDTO(url);
    }

    @Transactional
    public OAuthCallbackResponseDTO exchangeCodeForToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            HubSpotToken token = restTemplate.postForObject(tokenUrl, request, HubSpotToken.class);
            if (token == null) {
                throw new RuntimeException("Resposta inválida do HubSpot");
            }

            token.setIssuedAt(LocalDateTime.now());
            tokenRepository.save(token);

            return new OAuthCallbackResponseDTO(token.getAccessToken(), "Token obtido com sucesso");
        } catch (HttpClientErrorException e) {
            logger.error("Erro ao trocar código por token: Status={}, Response={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erro ao trocar código por token: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Erro inesperado: {}", e.getMessage(), e);
            throw new RuntimeException("Erro inesperado: " + e.getMessage());
        }
    }

    public ContactResponseDTO createContact(ContactRequestDTO contactRequest) {
        HubSpotToken token = tokenRepository.findTopByOrderByIssuedAtDesc()
                .orElseThrow(() -> new RuntimeException("Nenhum token de acesso encontrado. Autentique-se primeiro."));

        Map<String, Object> properties = new HashMap<>();
        properties.put("firstname", contactRequest.firstname());
        properties.put("lastname", contactRequest.lastname());
        properties.put("email", contactRequest.email());

        Map<String, Object> body = new HashMap<>();
        body.put("properties", properties);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token.getAccessToken());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            HubSpotContactResponseDTO response = restTemplate.postForObject(contactsUrl, request, HubSpotContactResponseDTO.class);

            if (response == null || response.getId() == null) {
                throw new RuntimeException("Falha ao criar contato: resposta inválida do HubSpot");
            }
             return new ContactResponseDTO("Contato criado com sucesso", response.getId());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                logger.warn("Rate limit excedido ao criar contato: {}", e.getResponseBodyAsString());
                throw new RuntimeException("Rate limit excedido. Tente novamente mais tarde.");
            } else if (e.getStatusCode().value() == 401) {
                logger.error("Token expirado ou inválido: {}", e.getResponseBodyAsString());
                throw new RuntimeException("Token de acesso expirado ou inválido. Autentique-se novamente.");
            }
            logger.error("Erro ao criar contato: Status={}, Response={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erro ao criar contato: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar contato: {}", e.getMessage(), e);
            throw new RuntimeException("Erro inesperado: " + e.getMessage());
        }
    }

}
