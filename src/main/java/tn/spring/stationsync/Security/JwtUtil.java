package tn.spring.stationsync.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // Clé secrète d'au moins 256 bits (32 caractères)
    @Value("${JWT_SECRET}")
    private String jwtSecretEnv;   // read from environment

    private SecretKey SECRET_KEY;

    @PostConstruct
    public void init() {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(jwtSecretEnv);
        SECRET_KEY = io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
    }
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 heures

    // Génère le token JWT
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    // Crée le token avec claims, sujet, signature et expiration
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // Extrait le username
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Vérifie si le token est expiré
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Valide le token pour l'utilisateur donné
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // --- Méthodes utilitaires ---
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
