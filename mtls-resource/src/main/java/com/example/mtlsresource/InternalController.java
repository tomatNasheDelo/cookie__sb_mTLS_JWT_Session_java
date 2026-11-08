package com.example.mtlsresource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InternalController {
    @GetMapping("/internal/ping")
    public InternalResponse ping() {
        return new InternalResponse(
                "mTLS connection successful: server certificate validated and client certificate accepted",
                true);
    }
}
