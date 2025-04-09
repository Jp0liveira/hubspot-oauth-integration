package com.hubspot.oauth.exception;

public class InvalidSignatureException extends SecurityException {
    public InvalidSignatureException(String message) { super(message); }
}
