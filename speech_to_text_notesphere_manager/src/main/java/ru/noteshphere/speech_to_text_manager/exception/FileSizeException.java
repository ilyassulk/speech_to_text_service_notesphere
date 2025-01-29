package ru.noteshphere.speech_to_text_manager.exception;

public class FileSizeException extends FileException {
    public FileSizeException(String message) {
        super(message);
    }

    public FileSizeException() {
    }
}