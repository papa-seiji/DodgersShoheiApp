package com.example.dodgersshoheiapp.service;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout; // ← ★これを追加
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class MlbPostseasonService {

    private static final String MLB_POSTSEASON_API = "https://statsapi.mlb.com/api/v1/schedule?sportId=1&season=2025&gameTypes=W,D,L,R&hydrate=team";
    // private static final String MLB_POSTSEASON_API =
    // "https://statsapi.mlb.com/api/v1/schedule?sportId=1&season=2025&gameType=W,D,L,F";

    public Map<String, Object> fetchPostseasonData() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Timeoutオブジェクトを使用（5秒）
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(Timeout.ofSeconds(5))
                    .setResponseTimeout(Timeout.ofSeconds(5))
                    .build();

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(config)
                    .build();

            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

            RestTemplate restTemplate = new RestTemplate(factory);
            Map<?, ?> data = restTemplate.getForObject(MLB_POSTSEASON_API, Map.class);

            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }
}
