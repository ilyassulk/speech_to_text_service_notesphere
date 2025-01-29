package ru.noteshphere.speech_to_text_manager.exception;

public class FileException extends RuntimeException {
    public FileException(String message) {
        super(message);
    }

    public FileException() {
    }
}