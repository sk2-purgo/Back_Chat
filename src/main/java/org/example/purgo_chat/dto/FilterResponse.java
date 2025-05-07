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
        boolean isAbusive   = false;
        String  rewritten   = null;
        String  original    = null;

        if (apiResponse != null) {

            /* ---- 1) 신규 스펙 : 최상위 키 ---- */
            Object abusiveFlag = apiResponse.get("is_abusive");
            if (abusiveFlag instanceof Boolean) {
                isAbusive = (Boolean) abusiveFlag;
            } else if (abusiveFlag != null) {
                // 문자열 "1" / "0" 대응
                isAbusive = "1".equals(abusiveFlag.toString());
            }

            original  = (String) apiResponse.get("original_text");
            rewritten = (String) apiResponse.get("rewritten_text");

            /* ---- 2) 구(舊) 스펙 : result.* & final_decision ---- */
            Object finalDecision = apiResponse.get("final_decision");
            if (finalDecision != null) {            // 1 / 0
                isAbusive = "1".equals(finalDecision.toString());
            }

            Map<String, Object> resultInner = (Map<String, Object>) apiResponse.get("result");
            if (resultInner != null) {
                if (original  == null) original  = (String) resultInner.get("original_text");
                if (rewritten == null) rewritten = (String) resultInner.get("rewritten_text");
            }
        }

        return FilterResponse.builder()
                .isAbusive(isAbusive)
                .originalText(original)
                .rewrittenText(rewritten)
                .build();
    }
}
