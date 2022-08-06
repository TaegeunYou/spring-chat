package chat.project.controller;

import chat.project.domain.ChatRequest;
import chat.project.domain.ChatResponse;
import chat.project.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

        final ChatRequest chatRequest = new ChatRequest();
        final DeferredResult<ChatResponse> deferredResult = new DeferredResult<>(null);   //Promise
        chatService.joinChatRoom(chatRequest, deferredResult);

//        deferredResult.onCompletion(() -> chatService)
//                        .onError(() -> chatService.error())
//                .onTimeOut(() -> chatService.timeOut());

        return deferredResult;
    }
}
