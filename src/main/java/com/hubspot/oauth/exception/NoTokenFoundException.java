package com.hubspot.oauth.exception;

public class NoTokenFoundException extends RuntimeException {
    public NoTokenFoundException(String message) { super(message); }
}