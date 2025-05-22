package org.example.purgo_chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.purgo_chat.dto.FilterResponse;
import org.example.purgo_chat.entity.ChatRoom;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadwordFilterService {

    @Value("${purgo.proxy.base-url}")
    private String proxyBaseUrl;

    @Value("${purgo.proxy.api-key}")
    private String proxyApiKey;

    private final ServerToProxyJwtService jwtService;
    private final ChatService chatService;
    private final @Qualifier("purgoRestTemplate") RestTemplate purgoRestTemplate;

    public FilterResponse filterMessage(String text, ChatRoom chatRoom, String sender) {
        try {
            log.info("FastAPI로 전송할 텍스트 (채팅): {}", text);

            Map<String, String> body = new HashMap<>();
            body.put("text", text);

            String jsonBody = jwtService.createJsonBody(body); // JSON 문자열 정렬 & 설정

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + proxyApiKey);
            headers.set("X-Auth-Token", jwtService.generateTokenFromJson(jsonBody));

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<Map> response =
                    purgoRestTemplate.postForEntity(proxyBaseUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                log.info("📦 FastAPI 응답 전체: {}", result);

                FilterResponse filterResponse = FilterResponse.fromApiResponse(result);
                String finalText = filterResponse.getDisplayText();
                log.info("욕설 여부: {}", filterResponse.isAbusive());
                log.info("최종 문장: {}", finalText);

                if (filterResponse.isAbusive()) {
                    chatService.incrementBadwordCount(chatRoom);
                }

                return filterResponse;          // 그대로 반환
            }
        } catch (Exception e) {
            log.error("❌ 욕설 분석 실패: {}", e.getMessage(), e);
        }

        // FastAPI 호출 실패 시 원문 그대로
        return FilterResponse.builder()
                .isAbusive(false)
                .originalText(text)
                .rewrittenText(text)
                .build();
    }
}
