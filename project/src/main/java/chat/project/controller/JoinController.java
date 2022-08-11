package chat.project.controller;

import chat.project.domain.ChatMessage;
import chat.project.domain.ChatRequest;
import chat.project.domain.ChatResponse;
import chat.project.domain.MessageType;
import chat.project.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
@Slf4j
public class JoinController {

    @Autowired
    private final ChatService chatService;

    @GetMapping("/join")
    @ResponseBody
    public DeferredResult<ChatResponse> joinRequest(HttpSession userSession) {

        String sessionId = userSession.getId();
        log.info("# Join Request. userSession Id: " + sessionId);

        final ChatRequest chatRequest = new ChatRequest(sessionId);
        final DeferredResult<ChatResponse> deferredResult = new DeferredResult<>(null);   //Promise
        chatService.joinChatRoom(chatRequest, deferredResult);

        //추가로 수행
//        deferredResult.onCompletion(() -> { System.out.println("a");});
        deferredResult.onError((throwable) -> chatService.cancelChatRoom(chatRequest));
        deferredResult.onTimeout(() -> chatService.timeOut(chatRequest));

        return deferredResult;
    }

    @GetMapping("/cancel")
    @ResponseBody
    public ResponseEntity<Void> cancelChatRoom(HttpSession userSession) {
        String sessionId = userSession.getId();
        log.info("# Cancel request. session id: {}", sessionId);

        final ChatRequest chatRequest = new ChatRequest(sessionId);
        chatService.cancelChatRoom(chatRequest);

        return ResponseEntity.ok().build();
    }

    @MessageMapping("/chat.message/{chatRoomId}")
    public void sendMessage(@DestinationVariable("chatRoomId") String chatRoomId, @Payload ChatMessage chatMessage) {
        log.info("Request Message. room id: {} | chat message: {}", chatRoomId, chatMessage);
        if (!StringUtils.hasText(chatMessage.getMessage())) {
            return;
        }

        if (chatMessage.getMessageType() == MessageType.CHAT) {
            chatService.sendMessage(chatRoomId, chatMessage);
        }
    }

}
