package ru.noteshphere.speech_to_text_processor.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.noteshphere.speech_to_text_processor.model.SpeechToTextRequest;

public interface SpeechToTextRequestRepository extends MongoRepository<SpeechToTextRequest, String> {
}