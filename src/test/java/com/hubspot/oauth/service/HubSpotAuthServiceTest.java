package com.hubspot.oauth.service;

import com.hubspot.oauth.dto.*;
import com.hubspot.oauth.exception.NoTokenFoundException;
import com.hubspot.oauth.exception.TokenExchangeException;
import com.hubspot.oauth.repository.HubSpotTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HubSpotAuthServiceTest {

    @Mock
    private HubSpotTokenRepository tokenRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private HubSpotAuthService authService;


    @BeforeEach
    void setUp() {
        authService = new HubSpotAuthService(tokenRepository);
        ReflectionTestUtils.setField(authService, "clientId", "testClientId");
        ReflectionTestUtils.setField(authService, "clientSecret", "testClientSecret");
        ReflectionTestUtils.setField(authService, "redirectUri", "http://localhost/callback");
        ReflectionTestUtils.setField(authService, "scopes", "scope1, scope2");
        ReflectionTestUtils.setField(authService, "authorizeUrl", "http://auth.url");
        ReflectionTestUtils.setField(authService, "tokenUrl", "http://token.url");
        ReflectionTestUtils.setField(authService, "contactsUrl", "http://contacts.url");

        ReflectionTestUtils.setField(authService, "restTemplate", restTemplate);
    }

    @Test
    public void testGenerateAuthorizationUrl() {
        AuthorizationUrlResponseDTO response = authService.generateAuthorizationUrl();
        assertNotNull(response);
        String url = response.getAuthorizationUrl();
        assertTrue(url.contains("client_id=testClientId"));
        assertTrue(url.contains("redirect_uri=http://localhost/callback"));

    }

    @Test
    public void testExchangeCodeForTokenSuccess() {
        HubSpotTokenDTO dummyToken = new HubSpotTokenDTO(1L, "dummyToken", "dummyAccessToken", "dummyRefreshToken", 3600L, LocalDateTime.now());

        when(restTemplate.postForObject(
                eq("http://token.url"),
                any(HttpEntity.class),
                eq(HubSpotTokenDTO.class)
        )).thenReturn(dummyToken);

        OAuthCallbackResponseDTO response = authService.exchangeCodeForToken("dummyCode");

        assertNotNull(response);
        assertEquals("Token gravado em memória e obtido com sucesso!", response.getMessage());
        verify(tokenRepository, times(1)).save(any());
    }

    @Test
    void testExchangeCodeForTokenFailure() {
        Mockito.when(restTemplate.postForObject(
                Mockito.eq("http://token.url"),
                ArgumentMatchers.any(HttpEntity.class),
                Mockito.eq(HubSpotTokenDTO.class)
        )).thenThrow(new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Erro na troca de token".getBytes(),
                StandardCharsets.UTF_8
        ));

        assertThrows(TokenExchangeException.class, () -> authService.exchangeCodeForToken("dummyCode"));
    }

    @Test
    void testCreateContactSuccess() {
        HubSpotTokenDTO dummyToken = new HubSpotTokenDTO(1L, "dummyToken", "dummyAccessToken", "dummyRefreshToken", 3600L, LocalDateTime.now());
        Mockito.when(tokenRepository.findTopByOrderByIssuedAtDesc()).thenReturn(Optional.of(dummyToken));

        HubSpotContactResponseDTO hubSpotContactResponseDTO = new HubSpotContactResponseDTO();
        hubSpotContactResponseDTO.setId("12345");

        Mockito.when(restTemplate.postForObject(
                Mockito.eq("http://contacts.url"),
                ArgumentMatchers.any(HttpEntity.class),
                Mockito.eq(HubSpotContactResponseDTO.class)
        )).thenReturn(hubSpotContactResponseDTO);

        ContactRequestDTO contactRequest = new ContactRequestDTO("First", "Last", "email@example.com");
        ContactResponseDTO response = authService.createContact(contactRequest);

        assertNotNull(response);
        assertEquals("Contato criado com sucesso", response.message());
        assertEquals("12345", response.contactId());
    }

    @Test
    void testCreateContactNoTokenFound() {
        Mockito.when(tokenRepository.findTopByOrderByIssuedAtDesc()).thenReturn(Optional.empty());

        ContactRequestDTO contactRequest = new ContactRequestDTO("First", "Last", "email@example.com");
        assertThrows(NoTokenFoundException.class, () -> authService.createContact(contactRequest));
    }

    @Test
    void testCreateContactRateLimitExceeded() {
        HubSpotTokenDTO dummyToken = new HubSpotTokenDTO(1L, "dummyToken", "dummyAccessToken", "dummyRefreshToken", 3600L, LocalDateTime.now());
        Mockito.when(tokenRepository.findTopByOrderByIssuedAtDesc()).thenReturn(Optional.of(dummyToken));

        Mockito.when(restTemplate.postForObject(
                Mockito.eq("http://contacts.url"),
                ArgumentMatchers.any(HttpEntity.class),
                Mockito.eq(HubSpotContactResponseDTO.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        ContactRequestDTO contactRequest = new ContactRequestDTO("First", "Last", "email@example.com");
        assertThrows(HttpClientErrorException.class, () -> authService.createContact(contactRequest));
    }

    @Test
    void testCreateContactInvalidToken() {
        HubSpotTokenDTO dummyToken = new HubSpotTokenDTO(1L, "dummyToken", "dummyAccessToken", "dummyRefreshToken", 3600L, LocalDateTime.now());
        Mockito.when(tokenRepository.findTopByOrderByIssuedAtDesc()).thenReturn(Optional.of(dummyToken));

        Mockito.when(restTemplate.postForObject(
                Mockito.eq("http://contacts.url"),
                ArgumentMatchers.any(HttpEntity.class),
                Mockito.eq(HubSpotContactResponseDTO.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        ContactRequestDTO contactRequest = new ContactRequestDTO("First", "Last", "email@example.com");
        assertThrows(HttpClientErrorException.class, () -> authService.createContact(contactRequest));
    }

}