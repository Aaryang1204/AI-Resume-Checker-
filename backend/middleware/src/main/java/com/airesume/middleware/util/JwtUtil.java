package com.airesume.middleware.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/*
 * @Component → registers this class as a Spring bean.
 * Spring will create one instance and inject it wherever needed (AuthService, JwtFilter, etc.)
 *
 * JwtUtil handles everything JWT-related:
 *   1. Generating tokens (on login / register)
 *   2. Validating tokens (on every incoming request)
 *   3. Extracting claims from tokens (email, role, expiry)
 *
 * A JWT has three base64-encoded parts separated by dots:
 *   HEADER.PAYLOAD.SIGNATURE
 *   - Header   → algorithm used (HS256)
 *   - Payload  → the claims: email, role, issued-at, expiry
 *   - Signature → HMAC-SHA256(header + payload, secret) — tamper-proof
 */
@Component
public class JwtUtil {

    /*
     * @Value("${jwt.secret}") → reads the value from application.properties.
     * The secret key is used to sign and verify every token.
     * Must be kept private — anyone with this key can forge tokens.
     * In production, inject it via environment variable, not hardcoded in properties.
     */
    @Value("${jwt.secret}")
    private String secret;

    /*
     * Token validity in milliseconds.
     * Default = 86400000 ms = 24 hours (used if jwt.expiration is not set in properties).
     * After this time the token is expired and the user must log in again.
     */
    @Value("${jwt.expiration:86400000}")
    private long expirationMs;

    /*
     * Converts the raw string secret into a cryptographic key object.
     * Keys.hmacShaKeyFor() ensures the key is the right format for HMAC-SHA256 signing.
     * Called fresh each time (not cached) so it always reflects the injected secret.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }


    // -------------------------------------------------------------------------
    // Token generation
    // -------------------------------------------------------------------------

    /*
     * generateToken(UserDetails) → builds and signs a JWT for the given user.
     *
     * We embed two extra claims in the payload:
     *   "role" → e.g. "ROLE_CANDIDATE" — so downstream services can read the role
     *             from the token without hitting the database every time.
     *   "userId" → the user's UUID — useful when processing requests that need the user's ID
     *               (e.g. saving a resume upload linked to this user).
     *
     * UserDetails is the interface our User class implements — it gives us getUsername() (email)
     * and getAuthorities() (roles) without needing to cast to the User class.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Extract the first (and only) authority, e.g. "ROLE_CANDIDATE"
        claims.put("role", userDetails.getAuthorities()
                .iterator().next().getAuthority());

        return buildToken(claims, userDetails.getUsername());
    }

    /*
     * Overload that also accepts a UUID so we can embed userId in the token.
     * Call this version from AuthService so the frontend/other services can
     * read the userId directly from the JWT without a DB lookup.
     */
    public String generateToken(UserDetails userDetails, UUID userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role",   userDetails.getAuthorities().iterator().next().getAuthority());
        claims.put("userId", userId.toString());
        return buildToken(claims, userDetails.getUsername());
    }

    /*
     * Internal method that actually assembles the JWT.
     *
     * .claims()     → sets the custom payload fields (role, userId)
     * .subject()    → sets the "sub" field — typically the user's email
     * .issuedAt()   → timestamp of when the token was created
     * .expiration() → timestamp after which the token is invalid
     * .signWith()   → signs the token with our HMAC-SHA256 key
     * .compact()    → serialises everything into the final "header.payload.signature" string
     */
    private String buildToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }


    // -------------------------------------------------------------------------
    // Token validation
    // -------------------------------------------------------------------------

    /*
     * isTokenValid() → the main check called by JwtAuthenticationFilter on every request.
     *
     * Two conditions must both be true:
     *   1. The email in the token matches the user we loaded from DB
     *      (prevents using a token issued for user A to authenticate as user B)
     *   2. The token has not expired
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /*
     * Checks if the token's expiry timestamp is before the current time.
     * If true, the token is stale and the user must log in again.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    // -------------------------------------------------------------------------
    // Claims extraction — reading data out of the token
    // -------------------------------------------------------------------------

    /*
     * The "sub" (subject) claim — the email we set during generateToken().
     * Used by JwtAuthenticationFilter to look up the user in the DB.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /*
     * The "role" claim — e.g. "ROLE_CANDIDATE".
     * Useful in places where you need to check the role without loading the full User.
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /*
     * The "userId" claim — the UUID we embedded during generateToken(user, uuid).
     * Use this in controllers/services that need the current user's ID
     * without an extra DB query.
     */
    public UUID extractUserId(String token) {
        String userIdStr = extractClaim(token, claims -> claims.get("userId", String.class));
        return userIdStr != null ? UUID.fromString(userIdStr) : null;
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /*
     * Generic claim extractor.
     * Takes a function (claimsResolver) that maps the Claims object to whatever type you need.
     * All the specific extractors above delegate to this method.
     *
     * Example: extractClaim(token, Claims::getSubject) → returns the subject string
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /*
     * Parses and verifies the full token.
     *
     * .verifyWith()     → checks the signature using our secret key
     *                     If the token was tampered with, this throws SignatureException
     * .parseSignedClaims() → parses the JWT and returns a Jws<Claims> wrapper
     * .getPayload()     → unwraps to get the Claims map (all the key-value pairs in the payload)
     *
     * This method throws exceptions for:
     *   - Expired tokens        → ExpiredJwtException
     *   - Tampered signatures   → SignatureException
     *   - Malformed tokens      → MalformedJwtException
     * JwtAuthenticationFilter catches these and lets the request through unauthenticated.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
