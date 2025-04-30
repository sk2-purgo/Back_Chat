package org.example.purgo_chat.dto;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterResponse {
    private boolean isAbusive;
    private String rewrittenText;

    public static FilterResponse fromApiResponse(Map<String, Object> apiResponse) {
        boolean isAbusive = false;
        String rewrittenText = null;

        if (apiResponse != null) {
            Object decision = apiResponse.get("final_decision");
            isAbusive = decision != null && decision.toString().equals("1");

            Map<String, Object> resultInner = (Map<String, Object>) apiResponse.get("result");
            if (resultInner != null) {
                rewrittenText = (String) resultInner.get("rewritten_text");
            }
        }

        return FilterResponse.builder()
                .isAbusive(isAbusive)
                .rewrittenText(rewrittenText)
                .build();
    }
}