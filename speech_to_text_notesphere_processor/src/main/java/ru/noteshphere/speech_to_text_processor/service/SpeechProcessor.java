package ru.noteshphere.speech_to_text_processor.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.noteshphere.speech_to_text_processor.config.MinioConfig;
import ru.noteshphere.speech_to_text_processor.config.SaluteSpeechConfig;
import ru.noteshphere.speech_to_text_processor.exception.ProcessingException;
import ru.noteshphere.speech_to_text_processor.model.SpeechToTextRequest;
import ru.noteshphere.speech_to_text_processor.repository.SpeechToTextRequestRepository;

import java.io.InputStream;

@Slf4j
@Service
public class SpeechProcessor {

    public SpeechProcessor(SpeechToTextRequestRepository repository, WebClient webClient, MinioConfig minioConfig, SaluteSpeechConfig saluteSpeechConfig, TokenManager tokenManager, MinioClient minioClient) {
        this.repository = repository;
        this.webClient = webClient;
        this.minioConfig = minioConfig;
        this.saluteSpeechConfig = saluteSpeechConfig;
        this.tokenManager = tokenManager;
        this.minioClient = minioClient;
    }

    private final SpeechToTextRequestRepository repository;
    private final MinioClient minioClient;
    private final TokenManager tokenManager;
    private final SaluteSpeechConfig saluteSpeechConfig;
    private final MinioConfig minioConfig;
    private final WebClient webClient;

    @KafkaListener(topics = "${spring.kafka.topic.name}")
    public void processRequest(String requestId) {
        try {
            // 1. Обновляем статус на "proc"
            repository.findById(requestId).ifPresent(request -> {
                request.setStatus("proc");
                repository.save(request);
            });

            // 2. Получаем информацию о запросе
            SpeechToTextRequest request = repository.findById(requestId)
                    .orElseThrow(() -> new ProcessingException("Request not found: " + requestId));

            // 3. Загружаем файл из Minio
            InputStream audioStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(request.getFileId())
                            .build()
            );

            // 4. Отправляем на распознавание
            String result = recognizeAudio(audioStream.readAllBytes());

            // 5. Обновляем результат
            request.setStatus("ok");
            request.setResult(result);
            repository.save(request);

        } catch (Exception e) {
            log.error("Processing failed for request: {}", requestId, e);
            repository.findById(requestId).ifPresent(request -> {
                request.setStatus("err");
                request.setResult(e.getMessage());
                repository.save(request);
            });
        }
    }

    private String recognizeAudio(byte[] audioData) {
        return webClient.post()
                .uri(saluteSpeechConfig.getRecognizeUrl())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenManager.getAccessToken())
                .header("X-Request-ID", java.util.UUID.randomUUID().toString())
                .contentType(MediaType.parseMediaType("audio/ogg;codecs=opus"))
                .bodyValue(audioData)
                .retrieve()
                .bodyToMono(RecognitionResponse.class)
                .onErrorMap(e -> new ProcessingException("Recognition failed:" + e.getMessage()))
                .blockOptional()
                .map(RecognitionResponse::getCombinedResult)
                .orElseThrow(() -> new ProcessingException("Empty recognition response"));
    }

    @Data
    private static class RecognitionResponse {
        private String[] result;
        private Integer status;

        public String getCombinedResult() {
            return String.join(" ", result).trim();
        }
    }
}