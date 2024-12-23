package net.revature.project1.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {
    private final SecretKey secretKey;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Autowired
    public JwtTokenUtil(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public String generateToken(String userName, Map<String, Object> claims) {
        return Jwts.builder()
                .subject(userName)
                .claims(claims)
                .issuer("project1")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(expiration)))
                .signWith(secretKey)
                .compact();
    }

    public Boolean validateToken(String token, String username) throws SignatureException {
        try {
            Claims claims = getAllClaimsFromToken(token);
            String tokenUsername = claims.getSubject();
            Date expiration = claims.getExpiration();

            return tokenUsername.equals(username) && !expiration.before(new Date());
        } catch (Exception e) {
            System.out.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
}
