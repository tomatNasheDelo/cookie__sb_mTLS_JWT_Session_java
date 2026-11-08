package com.example.cookielab;

public record JwtResponse(String accessToken, String tokenType, long expiresInSeconds) { }
