package com.issuetalk.ai.service;

import com.issuetalk.ai.dto.ChatGPTRequest;
import com.issuetalk.ai.dto.ChatGPTResponse;
import com.issuetalk.exception.errorcode.OpenAIErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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

    private String generate(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw OpenAIErrorCode.INVALID_ARGUMENT.defaultException();
        }

        ChatGPTRequest request = new ChatGPTRequest(model, prompt);

        // 헤더 설정 (Authorization 포함)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);  // <-- 중요!

        HttpEntity<ChatGPTRequest> entity = new HttpEntity<>(request, headers);

        ChatGPTResponse response;
        try {
            response = restTemplate.postForObject(apiURL, entity, ChatGPTResponse.class);
        } catch (Exception e) {
            throw OpenAIErrorCode.FAILED_TO_GENERATE.defaultException(e);
        }

        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw OpenAIErrorCode.FAILED_TO_GENERATE.defaultException();
        }

        String result = response.getChoices().get(0).getMessage().getContent();
        return result == null ? "" : result.trim();
    }

    // 상대 입장 요약
    public String summarizeStance(@Valid String message) {
        String prompt = """
            다음 문장은 토론 중 발언입니다:
            "%s"

            이 발언이 주장하는 핵심 논리는 무엇인가요?
            간결하고 중립적으로 50자 이내로 요약하세요.
            """.formatted(message);

        return generate(prompt);
    }

    // 팩트체크 기능
    public String factCheckClaim(@Valid String claim) {
        String prompt = """
            다음 주장의 진위 여부를 검토해주세요:
            "%s"

            출처가 있다면 간단히 인용해 주세요. 출처 없으면 '출처 없음'이라 하세요.
            """.formatted(claim);

        return generate(prompt);
    }

    // 감정 완화 기능
    public String neutralizeExpression(@Valid String message) {
        String prompt = """
            다음 문장은 공격적일 수 있습니다:
            "%s"

            정중하고 중립적인 어투로 바꿔주세요.
            """.formatted(message);

        return generate(prompt);
    }
}
