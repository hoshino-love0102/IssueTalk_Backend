package com.issuetalk.ai.controller;

import com.issuetalk.ai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class OpenAiController {

    // OpenAiService를 주입받아 사용
    private final OpenAiService openAiService;

    // 토론 발언 요약 요청을 처리하는 엔드포인트
    @PostMapping("/summary")
    public String summarize(@RequestBody String message) {
        return openAiService.summarizeStance(message);
    }

    // 팩트체크 요청을 처리하는 엔드포인트
    @PostMapping("/fact-check")
    public String factCheck(@RequestBody String claim) {
        return openAiService.factCheckClaim(claim);
    }

    // 감정 표현 완화 요청을 처리하는 엔드포인트
    @PostMapping("/neutralize")
    public String neutralize(@RequestBody String message) {
        return openAiService.neutralizeExpression(message);
    }
}