package ru.noteshphere.speech_to_text_manager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileSizeException.class)
    public ResponseEntity<String> handleNotFound(FileSizeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File size error (max 1000 KB)");
    }

    @ExceptionHandler(FileTypeException.class)
    public ResponseEntity<String> handleNotFound(FileTypeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File type error (expected *.opus file)");
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
    }
}