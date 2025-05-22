package org.example.purgo_chat.dto;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class FilterResponse {
    private boolean isAbusive;
    private String rewrittenText;
    private String originalText;   // 👈 원문

    /** 욕설이면 대체문, 아니면 원문 */
    public String getDisplayText() {
        return isAbusive ? rewrittenText : originalText;
    }

    @SuppressWarnings("unchecked")
    public static FilterResponse fromApiResponse(Map<String, Object> apiResponse) {
        boolean isAbusive = false;
        String rewritten = null;
        String original = null;

        if (apiResponse != null) {
            // 최종 판단 기준은 final_decision
            Object finalDecision = apiResponse.get("final_decision");
            if (finalDecision != null) {
                String decisionStr = finalDecision.toString();
                isAbusive = "1".equals(decisionStr) || "true".equalsIgnoreCase(decisionStr);
            }

            // result 내 원문/대체문 추출
            Object resultObj = apiResponse.get("result");
            if (resultObj instanceof Map<?, ?> resultMap) {
                Object originalObj = resultMap.get("original_text");
                Object rewrittenObj = resultMap.get("rewritten_text");

                if (originalObj instanceof String) {
                    original = (String) originalObj;
                }
                if (rewrittenObj instanceof String) {
                    rewritten = (String) rewrittenObj;
                }
            }
        }

        return FilterResponse.builder()
                .isAbusive(isAbusive)
                .originalText(original)
                .rewrittenText(rewritten)
                .build();
    }
}
