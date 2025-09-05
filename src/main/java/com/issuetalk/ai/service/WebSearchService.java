package com.issuetalk.ai.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebSearchService {

    @Value("${tavily.api-key:}")
    private String tavilyApiKey;

    private final RestTemplate restTemplate;

    @Data
    public static class WebDoc {
        private String title;
        private String url;
        private String snippet;
    }

    public List<WebDoc> search(String query, int maxResults) {
        if (tavilyApiKey == null || tavilyApiKey.isBlank()) return List.of();

        String endpoint = "https://api.tavily.com/search";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> reqBody = Map.of(
                "api_key", tavilyApiKey,
                "query", query,
                "max_results", Math.max(1, Math.min(maxResults, 8)),
                "include_answer", false
        );

        ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                new HttpEntity<>(reqBody, headers),
                new ParameterizedTypeReference<>() {}
        );

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) return List.of();

        Object results = resp.getBody().get("results");
        if (!(results instanceof List<?> list)) return List.of();

        List<WebDoc> docs = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof Map<?, ?> m) {
                WebDoc d = new WebDoc();

                // get + null 처리
                Object titleObj = m.get("title");
                Object urlObj   = m.get("url");
                Object contObj  = m.get("content");

                d.setTitle(titleObj != null ? titleObj.toString() : "");
                d.setUrl(urlObj != null ? urlObj.toString() : "");
                d.setSnippet(contObj != null ? contObj.toString() : "");

                if (!d.getUrl().isBlank()) docs.add(d);
            }
        }
        return docs;
    }
}
