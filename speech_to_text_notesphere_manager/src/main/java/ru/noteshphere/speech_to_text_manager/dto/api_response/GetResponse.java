package ru.noteshphere.speech_to_text_manager.dto.api_response;

public record GetResponse(String request_id, String status, String result, String error) {}