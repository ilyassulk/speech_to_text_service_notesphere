package ru.noteshphere.speech_to_text_processor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "salute.speech")
public class SaluteSpeechConfig {
    private String authUrl;
    private String recognizeUrl;
    private String scope;
    private String clientId;
    private String clientSecret;
    private String rquid;
    private String language;
    private String model;
    private String credentials;
    private int sampleRate;
    private int channelsCount;

    public String getAuthUrl() {
        return authUrl;
    }

    public String getRecognizeUrl() {
        return recognizeUrl;
    }

    public String getScope() {
        return scope;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRquid() {
        return rquid;
    }

    public String getLanguage() {
        return language;
    }

    public String getModel() {
        return model;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getChannelsCount() {
        return channelsCount;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public void setRecognizeUrl(String recognizeUrl) {
        this.recognizeUrl = recognizeUrl;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setRquid(String rquid) {
        this.rquid = rquid;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void setChannelsCount(int channelsCount) {
        this.channelsCount = channelsCount;
    }
}