package chat.project.domain;

import lombok.Data;

@Data
public class ChatRequest {

    private String sessionId;

    public ChatRequest(String sessionID) {
        this.sessionId = sessionID;
    }
}
