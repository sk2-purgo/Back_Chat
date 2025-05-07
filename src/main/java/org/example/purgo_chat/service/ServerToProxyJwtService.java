package org.example.purgo_chat.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ServerToProxyJwtService {

    @Value("${server-to-proxy.jwt.secret}")
    private String secretKeyString;

    @Value("${server-to-proxy.jwt.expiration}")
    private long expirationMillis;

    private Key secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    public String generateTokenFromJson(String jsonBody) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp  = new Date(nowMillis + expirationMillis);

        String bodyHash = DigestUtils.sha256Hex(jsonBody);

        Map<String,Object> claims = new HashMap<>();
        claims.put("iss", "purgo-chat");
        claims.put("hash", bodyHash);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}

