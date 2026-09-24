package lk.ridelink.account_service.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret-base64}") String secretBase64,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String email, int tokenVersion) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("ver", tokenVersion)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    public TokenClaims parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Number version = claims.get("ver", Number.class);
        return new TokenClaims(claims.getSubject(), version == null ? 0 : version.intValue());
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    public record TokenClaims(String email, int version) {
    }
}