package ru.noteshphere.speech_to_text_manager.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.noteshphere.speech_to_text_manager.dto.api_response.GetResponse;
import ru.noteshphere.speech_to_text_manager.exception.FileSizeException;
import ru.noteshphere.speech_to_text_manager.exception.FileTypeException;
import ru.noteshphere.speech_to_text_manager.exception.NotFoundException;
import ru.noteshphere.speech_to_text_manager.model.SpeechToTextRequest;
import ru.noteshphere.speech_to_text_manager.repository.SpeechToTextRequestRepository;

import java.util.UUID;

@Service
public class SpeechToTextService {

    private final SpeechToTextRequestRepository repository;
    private final MinioClient minioClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public SpeechToTextService(SpeechToTextRequestRepository repository, MinioClient minioClient, KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.minioClient = minioClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public SpeechToTextRequest processRequest(MultipartFile file) throws Exception {
        // Проверка размера файла (1000 КБ = 1024 * 1000 байт)
        if (file.getSize() > 1024 * 1000) {
            throw new FileSizeException();
        }

        // Проверка типа файла
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        Boolean isOpusFile = filename.toLowerCase().endsWith(".opus");
        if (contentType == null || (!contentType.equals("audio/ogg") && !contentType.equals("audio/opus") && !isOpusFile)) {
            throw new FileTypeException();
        }

        String requestId = UUID.randomUUID().toString();
        String fileName = requestId + ".opus";

        // Сохраняем файл в Minio
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType("audio/opus")
                        .build()
        );

        // Сохраняем запись в MongoDB
        SpeechToTextRequest request = new SpeechToTextRequest();
        request.setId(requestId);
        request.setStatus("wait");
        request.setFileId(fileName);
        request.setResult(null);
        repository.save(request);

        // Отправляем сообщение в Kafka
        kafkaTemplate.send("speech-to-text-requests", requestId);

        return request;
    }

    public GetResponse getResult(String requestId) throws Exception {
        SpeechToTextRequest request = repository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        GetResponse response;

        if (request.getStatus().equals("ok") || request.getStatus().equals("err")) {
            // Удаляем из MongoDB и Minio
            repository.deleteById(requestId);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(request.getFileId())
                            .build()
            );
        }

        response = switch (request.getStatus()) {
            case "ok" -> new GetResponse(requestId, "ok", request.getResult(), null);
            case "err" -> new GetResponse(requestId, "err", null, request.getResult());
            default -> new GetResponse(requestId, request.getStatus(), null, null);
        };

        return response;
    }
}