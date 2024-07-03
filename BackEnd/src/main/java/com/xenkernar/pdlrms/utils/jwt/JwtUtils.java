package com.xenkernar.pdlrms.utils.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.Authentication;

import java.util.Date;

public class JwtUtils {
    //expire time seconds , 7 days
    private static final long EXPIRE = 604800;
    //private key
    private static final String SECRET = "scauinfomationandcomputingscience";

    //generate jwt token
    public static String generateToken(Authentication authentication) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRE * 1000);
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .claim("authentication",authentication)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }


    //parse jwt token
    public static Claims parseToken(String token) {
        return Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
    }

}
