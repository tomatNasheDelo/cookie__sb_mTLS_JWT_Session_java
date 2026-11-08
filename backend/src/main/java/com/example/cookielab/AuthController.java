package com.example.cookielab;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final long TOKEN_LIFETIME_MILLIS = 15 * 60 * 1000L;

    private final String username;
    private final String password;
    private final SecretKey signingKey;

    public AuthController(
            @Value("${app.auth.username}") String username,
            @Value("${app.auth.password}") String password,
            @Value("${app.auth.jwt-secret-base64}") String secretBase64) {
        this.username = username;
        this.password = password;
        this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretBase64));
    }

    @PostMapping("/session/login")
    public AuthResponse sessionLogin(@RequestBody LoginRequest credentials, HttpSession session) {
        verifyCredentials(credentials);
        session.setAttribute("user", username);
        return new AuthResponse("Session created", username);
    }

    @GetMapping("/session/me")
    public AuthResponse sessionMe(HttpSession session) {
        Object user = session.getAttribute("user");
        if (user instanceof String authenticatedUser) {
            return new AuthResponse("Authenticated by server session", authenticatedUser);
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated session");
    }

    @PostMapping("/session/logout")
    public AuthResponse sessionLogout(HttpSession session) {
        session.invalidate();
        return new AuthResponse("Session invalidated", username);
    }

    @PostMapping("/jwt/login")
    public JwtResponse jwtLogin(@RequestBody LoginRequest credentials) {
        verifyCredentials(credentials);
        Date now = new Date();
        String token = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + TOKEN_LIFETIME_MILLIS))
                .signWith(signingKey)
                .compact();
        return new JwtResponse(token, "Bearer", 900);
    }

    @GetMapping("/jwt/me")
    public AuthResponse jwtMe(@RequestHeader(name = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Bearer token");
        }
        String token = authorization.substring("Bearer ".length());
        try {
            String subject = Jwts.parser().verifyWith(signingKey).build()
                    .parseSignedClaims(token).getPayload().getSubject();
            return new AuthResponse("Authenticated by verified JWT", subject);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired JWT");
        }
    }

    private void verifyCredentials(LoginRequest credentials) {
        if (credentials == null || !username.equals(credentials.username()) || !password.equals(credentials.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid demo credentials");
        }
    }
}
