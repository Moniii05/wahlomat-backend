package htw.wahlomat.wahlomat.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for generating and validating JSON Web Tokens (JWT).
 *
 * <p>This class is responsible for:
 * <ul>
 *     <li>Creating JWT tokens for authenticated users</li>
 *     <li>Embedding custom claims (role, userId)</li>
 *     <li>Extracting claims from tokens</li>
 *     <li>Validating token integrity and expiration</li>
 * </ul>
 *
 * <p>Tokens are signed using the HMAC-SHA256 algorithm (HS256).
 * The secret key is loaded from application properties.
 * If no secret is configured, a development fallback secret is used.
 *
 * <p><strong>Security Note:</strong>
 * The fallback secret is intended for development only and must NOT
 * be used in production environments.
 */
@Component
public class JwtUtil {


    // Fallback Secret für Development (wenn keine ENV Variable gesetzt)
    /**
     * Default fallback secret used only if no secret is configured.
     * <p>
     * Must be at least 256 bits long for HS256.
     */
    private static final String DEFAULT_SECRET = "MeinSuperGeheimesSecretDasMindestens256BitLangSeinMuss"; //nur für entwicklung

    /**
     * Secret configured via application properties.
     * If empty, DEFAULT_SECRET will be used.
     */
    @Value("${security.jwt.secret:}")  // ← Leer als Default
    private String configuredSecret;

    /**
     * Token expiration time in milliseconds.
     * Default: 86400000 ms (24 hours).
     */
    @Value("${security.jwt.expiration:86400000}")  // 24h als Default
    private long expirationTime;

    /**
     * Generates a signing key from the configured secret.
     *
     * @return HMAC-SHA signing key
     */
    private Key getSigningKey() {
        // Nutze configured secret falls vorhanden, sonst Fallback
        String secretToUse = (configuredSecret != null && !configuredSecret.isEmpty())
                ? configuredSecret
                : DEFAULT_SECRET;

        return Keys.hmacShaKeyFor(secretToUse.getBytes(StandardCharsets.UTF_8));
    }
    /**
     * Generates a JWT token for an authenticated user.
     *
     * @param userId unique ID of the user
     * @param role   role of the user (e.g. ADMIN, CANDIDATE)
     * @param email  email address (used as JWT subject)
     * @return signed JWT token as String
     */
    public String generateToken(Long userId, String role, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);
        return createToken(claims, email);
    }
    /**
     * Creates a signed JWT token with custom claims.
     *
     * @param claims  custom claims to embed
     * @param subject subject of the token (usually user email)
     * @return signed JWT token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    /**
     * Extracts the email (subject) from a JWT token.
     *
     * @param token JWT token
     * @return email stored as subject
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }
    /**
     * Extracts the role claim from a JWT token.
     *
     * @param token JWT token
     * @return role as String
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }
    /**
     * Extracts the user ID claim from a JWT token.
     *
     * @param token JWT token
     * @return user ID
     */
    public Long extractUserId(String token) {return extractAllClaims(token).get("userId", Long.class);}
    /**
     * Parses and validates all claims from the token.
     *
     * @param token JWT token
     * @return parsed claims
     * @throws SecurityException if token is expired or invalid
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith((SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new SecurityException("Token expired", e);
        } catch (JwtException e) {
            throw new SecurityException("Invalid token", e);
        }
    }
    /**
     * Validates a JWT token.
     *
     * <p>Validation checks:
     * <ul>
     *     <li>Token subject matches provided email</li>
     *     <li>Token is not expired</li>
     * </ul>
     *
     * @param token JWT token
     * @param email expected email
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token, String email) {
        final String extractedEmail = extractEmail(token);
        return (extractedEmail.equals(email) && !isTokenExpired(token));
    }
    /**
     * Checks whether the token is expired.
     *
     * @param token JWT token
     * @return true if expired, otherwise false
     */
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}