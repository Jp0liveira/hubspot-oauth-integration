package com.hubspot.oauth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class HubSpotWebhookValidator {

    @Value("${hubspot.client-secret}")
    private String clientSecret;

    public boolean validateWebhook(String signature, String signatureVersion, String requestBody) throws NoSuchAlgorithmException {
        if ("v1".equalsIgnoreCase(signatureVersion)) {
            String sourceString = requestBody != null ? clientSecret + requestBody : clientSecret;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(sourceString.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = bytesToHex(hashBytes);
            return calculatedSignature.equals(signature);
        }
        return false;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
