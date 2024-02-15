package com.eum.gateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;

@Component
@Slf4j
public class AuthUserAuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthUserAuthorizationHeaderFilter.Config> {
    Environment env;
    private static final String USER_ID = "userId";
    private static final String UID = "uid";
    private static final String ROLE = "role";
    private static final String BEARER_TYPE = "Bearer";
    private final Key key;
    public AuthUserAuthorizationHeaderFilter(Environment env){
        super(Config.class);
        this.env = env;
        byte[] keyBytes = Decoders.BASE64.decode("VlwEyVBsYt9V7zq57TejMnVUyzblYcfPQye08f7MGVA9XkHN");
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }


    public static class Config{


    }
    @Override
    public GatewayFilter apply(Config config) {
        return(exchange, chain) ->{
            ServerHttpRequest request =  exchange.getRequest();
//            ServerHttpResponse response = exchange.getResponse();

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange,"no authorization header", HttpStatus.UNAUTHORIZED);
            }
            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String jwt = authorizationHeader.replace(BEARER_TYPE, "");
            log.info(jwt);
//            if (!isJwtValid(jwt)) {
//                return onError(exchange, "JWT token is not valid", HttpStatus.UNAUTHORIZED);
//            }
            Claims claims = isJwtValid(jwt);
            if(!claims.get(ROLE).equals("ROLE_USER") ){
                return onError(exchange,"프로필 , 계좌 미생성 유저",HttpStatus.FORBIDDEN);
            }
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("userId", claims.get(USER_ID, Long.class).toString())
                    .build();

            // 수정된 요청으로 체인 실행
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
//            return chain.filter(exchange);
        };
    }
    private Claims isJwtValid(String jwt) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt).getBody();
            log.info(claims.get(USER_ID,Long.class).toString());
            return claims;
        }catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
            // 401 Unauthorized
            throw e;
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
            // 401 Expired
            throw e;
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
            // 400 Bad Request
            throw e;
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
            // 400 Bad Request
            throw e;
        }
    }
    //Mono, Flux -> spring webFlux : 클라이언트에서 요청이 들어왔을 때 반환 시켜줌
    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        log.error(error);
        return response.setComplete();
    }

}
