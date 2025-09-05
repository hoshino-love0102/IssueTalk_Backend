package com.issuetalk.ai.controller;

import com.issuetalk.ai.dto.request.TextRequest;
import com.issuetalk.ai.dto.response.AnalyzeResponse;
import com.issuetalk.ai.dto.response.SummaryResponse;
import com.issuetalk.ai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class OpenAiController {

    private final OpenAiService openAiService;

    @PostMapping(
            value = "/summary",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SummaryResponse summarize(@RequestBody TextRequest req) {
        return openAiService.summarizeStance(req.getMessage());
    }

    @PostMapping(
            value = "/clean-check",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public AnalyzeResponse cleanAndCheck(@RequestBody TextRequest req) {
        return openAiService.cleanAndFactCheck(req.getMessage());
    }
}
