package chat.project.domain;

import lombok.Data;

@Data
public class ChatResponse {
    private ResponseResultStatus responseResultStatus;
    private String chatRoomId;
    private String sessionId;

    public ChatResponse() {

    }

    public ChatResponse(ResponseResultStatus responseResultStatus, String chatRoomId, String sessionId) {
        this.responseResultStatus = responseResultStatus;
        this.chatRoomId = chatRoomId;
        this.sessionId = sessionId;
    }

    public enum ResponseResultStatus {
        SUCCESS, CANCEL, TIMEOUT
    }
}
