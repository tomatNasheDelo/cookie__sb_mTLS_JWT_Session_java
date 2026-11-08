package com.example.cookielab;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/mtls")
public class MtlsController {
    private final RestTemplate mtlsRestTemplate;
    private final String resourceUrl;

    public MtlsController(
            RestTemplate mtlsRestTemplate,
            @Value("${app.mtls.resource-url}") String resourceUrl) {
        this.mtlsRestTemplate = mtlsRestTemplate;
        this.resourceUrl = resourceUrl;
    }

    @GetMapping("/ping")
    public MtlsResponse ping() {
        MtlsResponse response = mtlsRestTemplate.getForObject(
                resourceUrl + "/internal/ping", MtlsResponse.class);
        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "mTLS resource returned an empty response");
        }
        return response;
    }
}
