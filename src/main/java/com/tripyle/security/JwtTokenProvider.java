package com.tripyle.security;

import com.tripyle.config.EnvConfig;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static java.util.Objects.isNull;

@DependsOn("env")
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private String secretKey = EnvConfig.jwtSecret();

    private static final long accessTokenValidMillisecond = 604800000; //1년
//    private static final long accessTokenValidMillisecond = 1000L * 60; //1분
    private static final long refreshTokenValidMillisecond = 1000L * 60 * 60 * 24 * 3;

    private final UserDetailsService userDetailsService;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(String userId, List<String> userRoles) {
        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("roles", userRoles);
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenValidMillisecond))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String createRefreshToken() {
        Date now = new Date();

        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenValidMillisecond))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getUsername(token));
        if(isNull(userDetails)) {
            return null;
        }
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUsername(String token) {
        String username;
        try {
            username = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
        }
        catch(ExpiredJwtException e) {
            username = e.getClaims().getSubject();
            return username;
        }
        return username;
    }

    public String resolveToken(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getHeader("X-AUTH-TOKEN");
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        }
        catch (Exception e) {
            return false;
        }
    }
}
