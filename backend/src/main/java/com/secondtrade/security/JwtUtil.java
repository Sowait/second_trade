package com.secondtrade.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
  private final byte[] secretBytes;
  private final long accessTtlSeconds;
  private final long refreshTtlSeconds;

  public JwtUtil(
    @Value("${app.jwt.secret}") String secret,
    @Value("${app.jwt.access-ttl-seconds:86400}") long accessTtlSeconds,
    @Value("${app.jwt.refresh-ttl-seconds:2592000}") long refreshTtlSeconds
  ) {
    this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    this.accessTtlSeconds = accessTtlSeconds;
    this.refreshTtlSeconds = refreshTtlSeconds;
  }

  public String issueAccessToken(Long userId, String role, boolean isSuperuser) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("user_id", userId);
    claims.put("role", role);
    claims.put("is_superuser", isSuperuser ? 1 : 0);
    long now = System.currentTimeMillis();
    Date exp = new Date(now + accessTtlSeconds * 1000L);
    return Jwts.builder()
      .setClaims(claims)
      .setSubject(String.valueOf(userId))
      .setIssuedAt(new Date(now))
      .setExpiration(exp)
      .signWith(SignatureAlgorithm.HS256, secretBytes)
      .compact();
  }

  public String issueRefreshToken(Long userId) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("user_id", userId);
    claims.put("type", "refresh");
    long now = System.currentTimeMillis();
    Date exp = new Date(now + refreshTtlSeconds * 1000L);
    return Jwts.builder()
      .setClaims(claims)
      .setSubject(String.valueOf(userId))
      .setIssuedAt(new Date(now))
      .setExpiration(exp)
      .signWith(SignatureAlgorithm.HS256, secretBytes)
      .compact();
  }

  public Long parseUserId(String token) {
    Claims c = Jwts.parser().setSigningKey(secretBytes).parseClaimsJws(token).getBody();
    Object v = c.get("user_id");
    if (v == null) return null;
    try {
      return Long.valueOf(String.valueOf(v));
    } catch (Exception e) {
      return null;
    }
  }

  public Claims parseClaims(String token) {
    return Jwts.parser().setSigningKey(secretBytes).parseClaimsJws(token).getBody();
  }
}

