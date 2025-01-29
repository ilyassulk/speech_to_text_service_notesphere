package ru.noteshphere.speech_to_text_processor.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Setter
@Getter
@Document(collection = "speech_requests")
public class SpeechToTextRequest {
    @Id
    public String id;
    public String status;
    public String fileId;
    public String result;

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getFileId() {
        return fileId;
    }

    public String getResult() {
        return result;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public void setResult(String result) {
        this.result = result;
    }
}