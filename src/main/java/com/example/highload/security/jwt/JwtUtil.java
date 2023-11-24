package com.example.highload.security.jwt;

import com.example.highload.model.inner.User;
import io.jsonwebtoken.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.token.secret}")
    private String jwtSecret;

    @Value("${jwt.token.expired}")
    private int jwtExpiration;

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRole());

        Date issuedDate = new Date();
        Date expiredDate = new Date((new Date()).getTime() + jwtExpiration);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getLogin())
                .setIssuedAt(issuedDate)
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    private Claims getClaimsFromToken(String token){
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
    }

    public String getLoginFromJwtToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public List<String> getRoleFromJwtToken(String token) {
        return getClaimsFromToken(token).get("roles", List.class);
    }


}
