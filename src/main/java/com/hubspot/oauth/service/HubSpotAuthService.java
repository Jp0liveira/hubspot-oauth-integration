package com.hubspot.oauth.service;

import com.hubspot.oauth.converter.HubSpotConverter;
import com.hubspot.oauth.dto.*;
import com.hubspot.oauth.exception.InvalidTokenException;
import com.hubspot.oauth.exception.NoTokenFoundException;
import com.hubspot.oauth.exception.RateLimitExceededException;
import com.hubspot.oauth.exception.TokenExchangeException;
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
                .queryParam("scope", scopes.replace(", ", " "))
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

        try {
            HubSpotTokenDTO token = restTemplate.postForObject(
                    tokenUrl,
                    new HttpEntity<>(params, headers),
                    HubSpotTokenDTO.class
            );

            tokenRepository.save(HubSpotConverter.converter(token));
            return new OAuthCallbackResponseDTO(token.accessToken(), "Token gravado em memória e obtido com sucesso!");

        } catch (HttpClientErrorException e) {
            throw new TokenExchangeException("Falha na troca de código por token: " + e.getResponseBodyAsString());
        }
    }


    public ContactResponseDTO createContact(ContactRequestDTO contactRequest) {
        HubSpotTokenDTO token = tokenRepository.findTopByOrderByIssuedAtDesc()
                .orElseThrow(() -> new NoTokenFoundException("Nenhum token encontrado"));

        Map<String, Object> body = Map.of(
                "properties", Map.of(
                        "firstname", contactRequest.firstname(),
                        "lastname", contactRequest.lastname(),
                        "email", contactRequest.email()
                )
        );

        try {
            HubSpotContactResponseDTO response = restTemplate.postForObject(
                    contactsUrl,
                    new HttpEntity<>(body, createAuthHeaders(token.accessToken())),
                    HubSpotContactResponseDTO.class
            );

            return new ContactResponseDTO("Contato criado com sucesso", response.getId());

        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new RateLimitExceededException("Limite de requisições excedido");
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new InvalidTokenException("Token inválido ou expirado");
        }
    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

}
