package com.eum.gateway;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Hidden
public class HealthCheckController {
    @GetMapping()
    public String healthCheck(){
        return "ok";
    }

}
