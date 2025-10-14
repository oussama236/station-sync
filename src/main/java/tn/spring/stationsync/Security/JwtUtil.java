package tn.spring.stationsync.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;   // <— add this import
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecretProp;

    private SecretKey SECRET_KEY;

    private static final long EXPIRATION_TIME = 1000L * 60 * 60 * 10; // 10 hours
    private static final long CLOCK_SKEW_SECONDS = 60L;

    @PostConstruct
    public void init() {
        if (jwtSecretProp == null || jwtSecretProp.isBlank()) {
            throw new IllegalStateException("jwt.secret is missing. Provide it via application-local.properties or env JWT_SECRET.");
        }

        // Trim to avoid trailing spaces/newlines in properties
        final String secret = jwtSecretProp.trim();

        byte[] keyBytes;
        try {
            // Try Base64 first (VM case)
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (DecodingException | IllegalArgumentException e) {   // <— catch both
            // Not Base64? Use raw bytes (local dev case)
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret must be at least 32 bytes (256 bits).");
        }

        SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String normalized = normalizeToken(token);
        String username = extractUsername(normalized);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(normalized);
    }

    public String extractUsername(String token) {
        return extractClaim(normalizeToken(token), Claims::getSubject);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(normalizeToken(token)).before(new Date());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .setAllowedClockSkewSeconds(CLOCK_SKEW_SECONDS)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    private String normalizeToken(String token) {
        if (token == null) return null;
        String t = token.trim();
        if (t.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return t.substring(7).trim();
        }
        return t;
    }
}
