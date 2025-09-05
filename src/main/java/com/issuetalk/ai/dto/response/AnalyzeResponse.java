package com.issuetalk.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyzeResponse {
    private String neutralized;
    private FactCheck factCheck = new FactCheck(); // NPE 방지

    @Getter @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FactCheck {
        private String verdict; // "참" | "거짓" | "판단불가"
        private String reason; // 한 문장
        private List<String> citations = new ArrayList<>();
    }
}
