package com.example.highload.security.jwt;

import com.example.highload.model.inner.User;
import com.example.highload.services.UserService;
import com.example.highload.utils.DataTransformer;
import io.jsonwebtoken.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.token.secret}")
    private String jwtSecret;

    @Value("${jwt.token.expired}")
    private int jwtExpiration;

    @Autowired
    private UserService userService;

    DataTransformer dataTransformer = new DataTransformer();

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
//        List<String> rolesList = user.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.toList());
//        claims.put("roles", rolesList);

        claims.put("roles", user.getRole());
//        claims.put("login", user.getLogin());

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

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }

    public String resolveToken(String login) {
        User user = userService.findByLogin(login);
        String token = null;
        token = generateToken(user);

        user.setAuthToken(token);
        userService.saveUser(dataTransformer.userToDto(user));
        return token;
    }
}
