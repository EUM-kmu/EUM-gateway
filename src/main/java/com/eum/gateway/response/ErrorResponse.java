package com.eum.gateway.response;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class ErrorResponse {

    //Mono, Flux -> spring webFlux : 클라이언트에서 요청이 들어왔을 때 반환 시켜줌
    public static Mono<Void> onError(ServerWebExchange exchange, int status, String divisionCode, String resultMsg, String reason,HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add(org.springframework.http.HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE);

        String errorMessage = "{\"status\": " + status +
                ", \"divisionCode\": \"" + divisionCode +
                "\", \"resultMsg\": \"" + resultMsg +
                "\", \"errors\": \"" + "" +
                "\", \"reason\": \"" + reason + "\"}";

        DataBuffer buffer = response.bufferFactory().wrap(errorMessage.getBytes());

        return response.writeWith(Mono.just(buffer));
    }

}
