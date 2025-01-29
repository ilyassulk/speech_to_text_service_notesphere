package ru.noteshphere.speech_to_text_manager.repository;

import ru.noteshphere.speech_to_text_manager.model.SpeechToTextRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpeechToTextRequestRepository extends MongoRepository<SpeechToTextRequest, String> {
}