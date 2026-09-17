package com.dtorrez.gym.security.utils;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Date;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.micrium.bd.access.enuns.Parametro;

import com.dtorrez.gym.services.ParametroService;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

/**
 *
 * @author alepaco.maton
 */
@Component
public class JwtTokenUtil implements Serializable {

    private static final Logger log = LogManager.getLogger(JwtTokenUtil.class);

    public static final String KEY_TOKEN = "Authorization";
    public static final String IP_CLIENT = "Forwarded-For";//  Forwarded: for=192.0.2.60;proto=http;by=203.0.113.43
    public static final String FORM = "Referer";// Forwarded: for=192.0.2.60;proto=http;by=203.0.113.43
    public static final String ROUTE = "Route";

    private static final long serialVersionUID = -2550185165626007488L;



    @Autowired
    transient ParametroService parametroService; // 33646 coverity

    /*
     * @Value("${jwt.timelife}")
     * public String JWT_TOKEN_VALIDIT;
     */

    // expresado en horas
    // public Long TiempoValidacionToken=1l;

    @Value("${jwt.secret}")
    private String secret;

    // retrieve username from jwt token
    public String getUsernameFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return token;
        }

        return getClaimFromToken(token.substring(7), Claims::getSubject);
    }

    public String getRolNombreFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return token;
        }

        return (String) getAllClaimsFromToken(token.substring(7)).get("rol");
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        //log.info("getClaimFromToken ");
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // for retrieveing any information from token we will need the secret key
    private Claims getAllClaimsFromToken(String token) {
        //return Jwts.parser().setSigningKey(secret.getBytes(StandardCharsets.UTF_8)).parseClaimsJws(token).getBody();        
        return Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
        .build()
        .parseClaimsJws(token)
        .getBody();
    }
    
    // check if the token has expired
    public Boolean isTokenExpired(String token) {
        //final Date expiration = getExpirationDateFromToken(token.substring(7));      
        token = token.replace("Bearer ","");
        final Date expiration = getClaimFromToken(token, Claims::getExpiration);
        return expiration.before(new Date(System.currentTimeMillis()));
    }

    // generate token for user
    public String generateToken(String nombreUsuario, String rolName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", rolName);
        return doGenerateToken(claims, nombreUsuario);
    }

    // while creating the token -
    // 1. Define claims of the token, like Issuer, Expiration, Subject, and the ID
    // 2. Sign the JWT using the HS512 algorithm and secret key.
    // 3. According to JWS Compact
    // Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    // compaction of the JWT to a URL-safe string
    // para que token expire en minutos seria 30min/60 *1000 *60*60
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        Integer tokenTime = Integer.valueOf(parametroService.getParametroByNombre(Parametro.DelSistema.TOKEN_TIME.name()).getValor());
        /*return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (((tokenTime) * 1000) * 60)))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes(StandardCharsets.UTF_8)).compact();*/
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (((tokenTime) * 1000) * 60)))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256) // Firma el token con la clave y el algoritmo HS256
                .compact();
    }
}
