package com.hubspot.oauth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)

public class HubSpotWebhookValidatorTest {

    private HubSpotWebhookValidator validator;

    private final String testClientSecret = "testSecret";

    private String computeSha256Hex(String source) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(source.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    @BeforeEach
    public void setUp() {
        validator = new HubSpotWebhookValidator();
        ReflectionTestUtils.setField(validator, "clientSecret", testClientSecret);
    }

    @Test
    public void testValidateWebhookWithValidSignature() throws NoSuchAlgorithmException {
        String requestBody = "{\"key\":\"value\"}";
        String source = testClientSecret + requestBody;
        String expectedSignature = computeSha256Hex(source);
        String signatureVersion = "v1";

        boolean result = validator.validateWebhook(expectedSignature, signatureVersion, requestBody);
        assertTrue(result, "Deve retornar true para assinatura correta");
    }

    @Test
    public void testValidateWebhookWithInvalidSignature() throws NoSuchAlgorithmException {
        String requestBody = "{\"key\":\"value\"}";
        String wrongSignature = "0000000000000000000000000000000000000000000000000000000000000000";
        String signatureVersion = "v1";

        boolean result = validator.validateWebhook(wrongSignature, signatureVersion, requestBody);
        assertFalse(result, "Deve retornar false para assinatura incorreta");
    }

    @Test
    public void testValidateWebhookWithWrongSignatureVersion() throws NoSuchAlgorithmException {
        String requestBody = "{\"key\":\"value\"}";
        String source = testClientSecret + requestBody;
        String expectedSignature = computeSha256Hex(source);
        String signatureVersion = "v2";

        boolean result = validator.validateWebhook(expectedSignature, signatureVersion, requestBody);
        assertFalse(result, "Deve retornar false para versão de assinatura diferente de v1");
    }

    @Test
    public void testValidateWebhookWithNullRequestBody() throws NoSuchAlgorithmException {
        String requestBody = null;
        String expectedSignature = computeSha256Hex(testClientSecret);
        String signatureVersion = "v1";

        boolean result = validator.validateWebhook(expectedSignature, signatureVersion, requestBody);
        assertTrue(result, "Deve retornar true quando requestBody é nulo e a assinatura está correta");
    }

}
