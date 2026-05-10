package com.ryannoah.auction.utilities;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SystemController {

    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
                "service", "user-service",
                "status", "ok",
                "message", "User service is running"
        );
    }
}
