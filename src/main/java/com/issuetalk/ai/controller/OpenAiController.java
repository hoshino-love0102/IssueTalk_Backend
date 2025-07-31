package com.issuetalk.ai.controller;

import com.issuetalk.ai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class OpenAiController {

    private final OpenAiService openAiService;

    @PostMapping("/summary")
    public String summarize(@RequestBody String message) {
        return openAiService.summarizeStance(message);
    }

    @PostMapping("/fact-check")
    public String factCheck(@RequestBody String claim) {
        return openAiService.factCheckClaim(claim);
    }

    @PostMapping("/neutralize")
    public String neutralize(@RequestBody String message) {
        return openAiService.neutralizeExpression(message);
    }
}
