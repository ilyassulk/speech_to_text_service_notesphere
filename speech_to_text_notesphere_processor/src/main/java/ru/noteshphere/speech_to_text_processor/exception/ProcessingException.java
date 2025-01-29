package ru.noteshphere.speech_to_text_processor.exception;

public class ProcessingException extends RuntimeException {
    public ProcessingException(String message) {
        super(message);
    }
}