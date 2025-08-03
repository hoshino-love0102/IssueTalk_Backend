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

    @Value("${openai.model}") // application.yml에서 모델 이름 주입
    private String model;

    @Value("${openai.api.url}") // API 호출 URL 주입
    private String apiURL;

    @Value("${openai.api-key}") // OpenAI API 키 주입
    private String openAiApiKey;

    private final RestTemplate restTemplate; // HTTP 통신용 객체

    // GPT 응답 생성 메서드
    private String generate(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw OpenAIErrorCode.INVALID_ARGUMENT.defaultException(); // 유효성 검사
        }

        ChatGPTRequest request = new ChatGPTRequest(model, prompt); // 요청 생성

        HttpHeaders headers = new HttpHeaders(); // HTTP 헤더 생성
        headers.setContentType(MediaType.APPLICATION_JSON); // JSON 타입 지정
        headers.setBearerAuth(openAiApiKey); // Bearer 인증 헤더

        HttpEntity<ChatGPTRequest> entity = new HttpEntity<>(request, headers); // 요청 본문 생성

        ChatGPTResponse response;
        try {
            response = restTemplate.postForObject(apiURL, entity, ChatGPTResponse.class); // 요청 전송
        } catch (Exception e) {
            throw OpenAIErrorCode.FAILED_TO_GENERATE.defaultException(e); // 실패 시 예외
        }

        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw OpenAIErrorCode.FAILED_TO_GENERATE.defaultException(); // 결과 없음 처리
        }

        String result = response.getChoices().get(0).getMessage().getContent(); // 응답 내용 추출
        return result == null ? "" : result.trim(); // 결과 반환
    }

    // 발언 요약
    public String summarizeStance(@Valid String message) {
        String prompt = """
            다음 문장은 토론 중 발언입니다:
            "%s"

            이 발언이 주장하는 핵심 논리는 무엇인가요?
            간결하고 중립적으로 100자 이내로 요약하세요.
            """.formatted(message);

        return generate(prompt); // GPT 요청
    }

    // 팩트체크
    public String factCheckClaim(@Valid String claim) {
        String prompt = """
            다음 주장의 진위 여부를 검토해주세요:
            "%s"

            출처가 있다면 간단히 인용해 주세요.
            """.formatted(claim);

        return generate(prompt); // GPT 요청
    }

    // 감정 완화
    public String neutralizeExpression(@Valid String message) {
        String prompt = """
            다음 문장은 공격적일 수 있습니다:
            "%s"

            정중하고 중립적인 어투로 바꿔주세요.
            """.formatted(message);

        return generate(prompt); // GPT 요청
    }
}
