package com.issuetalk.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.issuetalk.ai.dto.request.ChatGPTRequest;
import com.issuetalk.ai.dto.response.AnalyzeResponse;
import com.issuetalk.ai.dto.response.ChatGPTResponse;
import com.issuetalk.ai.dto.response.SummaryResponse;
import com.issuetalk.exception.errorcode.OpenAIErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OpenAiService {

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiURL;

    @Value("${openai.api-key}")
    private String openAiApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final WebSearchService webSearchService;

    private static final String SYSTEM_RULES = """
        너는 공영방송 시사 프로그램 <100분 토론>을 여러 회 진행한 중립적 사회자다.
        - 요청한 출력 형식만 반환한다(사족/인사말 금지).
        - 정치적/이념적 편향 표현을 피한다.
        - 의미 왜곡 금지, 단정이 어려우면 '판단불가'.
        """;

    private String generateRaw(String prompt, double temperature) {
        if (prompt == null || prompt.isBlank()) {
            throw OpenAIErrorCode.INVALID_ARGUMENT.defaultException();
        }

        ChatGPTRequest request = new ChatGPTRequest(model, SYSTEM_RULES, prompt, temperature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<ChatGPTRequest> entity = new HttpEntity<>(request, headers);

        ChatGPTResponse response;
        try {
            response = restTemplate.postForObject(apiURL, entity, ChatGPTResponse.class);
        } catch (Exception e) {
            throw OpenAIErrorCode.FAILED_TO_GENERATE.defaultException(e);
        }

        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()
                || response.getChoices().get(0).getMessage() == null) {
            throw OpenAIErrorCode.FAILED_TO_GENERATE.defaultException();
        }

        String content = response.getChoices().get(0).getMessage().getContent();
        if (content == null) content = "";
        content = content.trim();

        if (content.startsWith("```")) {
            content = content
                    .replaceFirst("^```(?:json)?\\s*", "")
                    .replaceFirst("\\s*```\\s*$", "")
                    .trim();
        }
        return content;
    }

    private <T> T generateJson(String prompt, Class<T> type, double temperature) {
        String json = generateRaw(prompt, temperature);
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw OpenAIErrorCode.FAILED_TO_GENERATE.defaultException(
                    new IllegalStateException("Invalid JSON from model: " + json, e)
            );
        }
    }

    // 요약
    public SummaryResponse summarizeStance(@Valid String message) {
        String prompt = """
            역할: 토론 발언을 한 문장으로 요약한다.
            규칙:
            - 100자 이내, 평서문 한 문장.
            - 중립적/사실적 표현만 사용.
            출력은 JSON 한 줄:
            {"summary":"<요약문>"}
            입력:
            "%s"
            """.formatted(message);

        return generateJson(prompt, SummaryResponse.class, 0.2);
    }

    // 2) 순화 + 팩트체크(+출처)
    public AnalyzeResponse cleanAndFactCheck(@Valid String message) {
        var docs = webSearchService.search(message, 5);

        String contextBlock = docs.isEmpty() ? "(자료 없음)"
                : docs.stream()
                .map(d -> "- 제목: " + d.getTitle()
                        + "\n  URL: " + d.getUrl()
                        + "\n  요약: " + trim(d.getSnippet(), 400))
                .collect(Collectors.joining("\n\n"));

        String prompt = """
            [목표]
            (1) 입력 문장에서 욕설/비속어/모욕/공격 표현만 최소 수정하여 의미는 보존한다.
            (2) 아래 <참고 문헌> '내'에서만 사실 주장 여부를 간단히 판단하고, 사용한 근거의 URL을 citations 배열로 반환한다.

            [규칙]
            - 순화: 설교조/명령조/비하 표현 제거. 의도와 정보는 유지.
            - 팩트체크: 참고 문헌 밖의 정보/추정/창작 금지. 새 링크 만들지 말 것.
            - 참고 문헌이 충분치 않거나 모호하면 verdict="판단불가".
            - 결과는 JSON 한 줄, 스키마 고정:
            {
              "neutralized":"<순화 문장(들)>",
              "factCheck":{"verdict":"참|거짓|판단불가","reason":"<120자 이내 한 문장>","citations":["<URL1>","<URL2>"]}
            }
            - citations에는 실제로 참고한 위 URL만 넣는다. 없으면 빈 배열 [].

            [입력]
            "%s"

            [참고 문헌]
            %s
            """.formatted(message, contextBlock);

        AnalyzeResponse out = generateJson(prompt, AnalyzeResponse.class, 0.0);

        // 문헌이 전혀 없으면 안전 보정
        if (docs.isEmpty()) {
            out.getFactCheck().setCitations(List.of());
            if (!"판단불가".equals(out.getFactCheck().getVerdict())) {
                out.getFactCheck().setVerdict("판단불가");
                out.getFactCheck().setReason("참고 문헌이 없어 단정할 수 없습니다.");
            }
        }
        return out;
    }

    private static String trim(String s, int max) {
        if (s == null) return "";
        s = s.replaceAll("\\s+", " ").trim();
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
