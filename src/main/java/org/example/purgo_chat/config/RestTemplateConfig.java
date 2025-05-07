package org.example.purgo_chat.config;

import lombok.RequiredArgsConstructor;
import org.example.purgo_chat.service.ServerToProxyJwtService;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;

import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    @Value("${purgo.proxy.base-url}")
    private String proxyBaseUrl;

    @Value("${purgo.proxy.api-key}")
    private String apiKey;

    private final ServerToProxyJwtService jwtService;   // 👈 신규 Bean

    @Bean(name = "purgoRestTemplate")
    public RestTemplate purgoRestTemplate(RestTemplateBuilder builder) {

        ClientHttpRequestInterceptor authInterceptor = (req, body, ex) -> {
            // Bearer API‑Key
            req.getHeaders().set("Authorization", "Bearer " + apiKey);

            // 서버‑to‑proxy JWT : body 해시 기반
            String bodyStr = new String(body, StandardCharsets.UTF_8);
            String jwt = jwtService.generateTokenFromJson(bodyStr);
            req.getHeaders().set("X-Auth-Token", jwt);

            return ex.execute(req, body);
        };

        return builder
                .rootUri(proxyBaseUrl)      // base-url 통일
                .additionalInterceptors(authInterceptor)
                .build();
    }
}
