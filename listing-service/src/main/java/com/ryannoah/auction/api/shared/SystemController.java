package com.ryannoah.auction.api.shared;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SystemController {

    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
                "service", "listing-service",
                "status", "ok",
                "message", "Listing service is running"
        );
    }
}
