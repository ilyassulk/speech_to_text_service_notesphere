package ru.noteshphere.speech_to_text_processor.service;


import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import ru.noteshphere.speech_to_text_processor.config.SaluteSpeechConfig;
import ru.noteshphere.speech_to_text_processor.exception.SaluteSpeechException;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenManager {
    private static final Duration TOKEN_EXPIRY = Duration.ofMinutes(20);

    private final SaluteSpeechConfig config;
    private final WebClient webClient;

    private String accessToken;
    private Instant tokenExpiry;

    @PostConstruct
    @Scheduled(fixedRate = 30 * 60 * 1000) // Обновление каждые 30 минут
    public synchronized void refreshToken() {
        try {
            String credentials = config.getCredentials();

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("scope", config.getScope());

            accessToken = webClient.post()
                    .uri(config.getAuthUrl())
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                    .header("RqUID", config.getRquid())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .blockOptional()
                    .orElseThrow(() -> new SaluteSpeechException("Empty token response"))
                    .getAccess_token();

            tokenExpiry = Instant.now().plus(TOKEN_EXPIRY);
            log.info("Salute Speech token refreshed");
        } catch (Exception e) {
            log.error("Failed to refresh token", e);
            throw new SaluteSpeechException("Token refresh failed:" + e);
        }
    }

    public synchronized String getAccessToken() {
        if (Instant.now().isAfter(tokenExpiry)) {
            refreshToken();
        }
        return accessToken;
    }

    @Data
    private static class TokenResponse {
        private String access_token;
    }
}