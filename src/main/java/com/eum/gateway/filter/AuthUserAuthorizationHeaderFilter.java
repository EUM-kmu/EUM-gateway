package com.eum.gateway.filter;

import com.eum.gateway.jwt.JwtService;
import com.eum.gateway.response.ErrorResponse;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.buffer.DataBuffer;
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
/**
 * 햇살 서버 접근 전용 필터
 */
public class AuthUserAuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthUserAuthorizationHeaderFilter.Config> {
    Environment env;
    private static final String USER_ID = "userId";
    private static final String UID = "uid";
    private static final String ROLE = "role";
    private static final String BEARER_TYPE = "Bearer";
    private final Key key;
    private static final String PREVIOUS_USERID = "previousUserId";
    private static final String DELETED = "deleted";
    @Autowired
    private JwtService jwtService;
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
                return ErrorResponse.onError(exchange,401,"G021", "no authorization header","헤더에 값을 넣어주세요", HttpStatus.UNAUTHORIZED);
            }
            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String jwt = authorizationHeader.replace(BEARER_TYPE, "");
            log.info(jwt);

            Claims claims;
            try {
                claims = jwtService.isJwtValid(jwt);
            } catch (RuntimeException e) {
                return ErrorResponse.onError(exchange,401,"G020","Invalid Token Exception", e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
            if(!claims.get(ROLE).equals("ROLE_USER") ){
                return ErrorResponse.onError(exchange,403,"G008","Forbidden Exception","프로필 , 계좌 미생성 유저",HttpStatus.FORBIDDEN);
            }
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("userId", claims.get(USER_ID, Long.class).toString())
                    .header("previousUserId", claims.get(PREVIOUS_USERID, Long.class).toString())
                    .build();

            // 수정된 요청으로 체인 실행
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
//            return chain.filter(exchange);
        };
    }

}
