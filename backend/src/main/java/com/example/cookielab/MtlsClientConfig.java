package com.example.cookielab;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Configuration
public class MtlsClientConfig {
    private final String keyStorePath;
    private final String trustStorePath;
    private final String storePassword;

    public MtlsClientConfig(
            @Value("${app.mtls.client-key-store}") String keyStorePath,
            @Value("${app.mtls.trust-store}") String trustStorePath,
            @Value("${app.mtls.store-password}") String storePassword) {
        this.keyStorePath = keyStorePath;
        this.trustStorePath = trustStorePath;
        this.storePassword = storePassword;
    }

    @Bean
    public RestTemplate mtlsRestTemplate() throws Exception {
        KeyStore keyStore = loadStore(keyStorePath);
        KeyStore trustStore = loadStore(trustStorePath);
        SSLContext sslContext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, storePassword.toCharArray())
                .loadTrustMaterial(trustStore, null)
                .build();
        var socketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext)
                .build();
        var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(socketFactory)
                .build();
        var httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }

    private KeyStore loadStore(String location)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        String path = location.startsWith("file:") ? location.substring("file:".length()) : location;
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream input = new FileInputStream(path)) {
            keyStore.load(input, storePassword.toCharArray());
        }
        return keyStore;
    }
}
