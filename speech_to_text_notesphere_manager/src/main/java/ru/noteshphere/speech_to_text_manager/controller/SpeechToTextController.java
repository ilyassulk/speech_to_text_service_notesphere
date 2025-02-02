package ru.noteshphere.speech_to_text_manager.controller;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.noteshphere.speech_to_text_manager.dto.api_response.*;
import ru.noteshphere.speech_to_text_manager.exception.FileException;
import ru.noteshphere.speech_to_text_manager.exception.NotFoundException;
import ru.noteshphere.speech_to_text_manager.service.SpeechToTextService;

@RestController
public class SpeechToTextController {

    private final ru.noteshphere.speech_to_text_manager.service.SpeechToTextService service;

    public SpeechToTextController(SpeechToTextService service) {
        this.service = service;
    }

    @PostMapping(value = "/request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            var request = service.processRequest(file);
            return ResponseEntity.ok(new PostResponse(request.getId(), request.getStatus()));
        } catch (FileException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to process request", e);
        }
    }

    @GetMapping("/result/{requestId}")
    public ResponseEntity<GetResponse> getResult(@PathVariable String requestId) {
        try {
            return ResponseEntity.ok(service.getResult(requestId));
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve result", e);
        }
    }
}