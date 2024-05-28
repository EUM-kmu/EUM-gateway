package com.eum.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class PreFlightCorsConfiguration {

    private static final String ALLOWED_HEADERS = "x-requested-with, Authorization, Content-Type";
    private static final String ALLOWED_METHODS = "GET, PUT, POST, DELETE, OPTIONS,PATCH";
    private static final String MAX_AGE = "3600";
    private static final String ALLOWED_CREDENTIALS = "true";

    // 허용할 Origin 목록을 HashSet으로 관리
    private static final Set<String> allowedOrigins = new HashSet<>();
    static {
        allowedOrigins.add("http://localhost:3000");
        allowedOrigins.add("http://localhost:58656"); // 타임뱅크 관리자 페이지
        allowedOrigins.add("https://hanmaeul.vercel.app");
        allowedOrigins.add("https://k-eum2023.web.app");
        // 다른 허용할 Origin 추가
    }

    @Bean
    public WebFilter corsFilter() {
        return (ServerWebExchange ctx, WebFilterChain chain) -> {

            ServerHttpRequest request = ctx.getRequest();

            // 요청이 Preflight 요청인지 확인
            if (CorsUtils.isPreFlightRequest(request)) {
                ServerHttpResponse response = ctx.getResponse();
                HttpHeaders headers = response.getHeaders();

                // 요청 Origin이 허용 목록에 있는지 확인
                String requestOrigin = request.getHeaders().getOrigin();
                if (requestOrigin != null && allowedOrigins.contains(requestOrigin)) {
                    // 허용된 Origin이라면 해당 Origin을 반환
                    headers.add("Access-Control-Allow-Origin", requestOrigin);
                    headers.add("Access-Control-Allow-Methods", ALLOWED_METHODS);
                    headers.add("Access-Control-Max-Age", MAX_AGE);
                    headers.add("Access-Control-Allow-Headers", ALLOWED_HEADERS);
                    headers.add("Access-Control-Allow-Credentials", ALLOWED_CREDENTIALS);

                    if (request.getMethod() == HttpMethod.OPTIONS) {
                        response.setStatusCode(HttpStatus.OK);
                        return Mono.empty();
                    }
                }
            }
            return chain.filter(ctx);
        };
    }
}
