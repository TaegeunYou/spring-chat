package chat.project.service;

import chat.project.domain.ChatRequest;
import chat.project.domain.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private ReentrantReadWriteLock lock;    //읽기 vs 읽기는 허용하지만 쓰기가 하나라도 있으면 늦게 온 요청은 lock 한다.
    private Map<ChatRequest, DeferredResult<ChatResponse>> waitingUsers;


    @PostConstruct
    private void setUp() {
//        this.waitingUsers = new LinkedHashMap<>();
//        this.lock = new ReentrantReadWriteLock();
//        this.connectedUsers = new ConcurrentHashMap<>();
    }

    public void joinChatRoom(ChatRequest request, DeferredResult<ChatResponse> deferredResult) {
        Thread currentThread = Thread.currentThread();
        logger.info("# Join chat room request. Name: {}, Id: {}", currentThread.getName(), currentThread.getId());

        if (request == null || deferredResult == null) {
            return;
        }

        try {
            waitingUsers.put(request, deferredResult);
        } finally {
            establishChatRoom();
        }
    }

    public void sendMessage() {

    }

    public String getDestination() {
        return "";
    }

    public void timeOut() {

    }

    public void error() {
    }

    public void establishChatRoom() {
        try {
            logger.info("Current waiting users size: " + waitingUsers.size());
            if (waitingUsers.size() < 2) {
                return;
            }

            Iterator<ChatRequest> iterator = waitingUsers.keySet().iterator();

            ChatRequest user1 = iterator.next();
            ChatRequest user2 = iterator.next();

            String chatRoomId = UUID.randomUUID().toString();

            //user1과 user2를 대기 목록에서 삭제시키고 채팅방에 넣어야 함.

            DeferredResult<ChatResponse> user1Result = waitingUsers.remove(user1);
            DeferredResult<ChatResponse> user2Result = waitingUsers.remove(user2);

            user1Result.setResult(new ChatResponse(ChatResponse.ResponseResultStatus.SUCCESS, chatRoomId, user1.getSessionId()));
            user2Result.setResult(new ChatResponse(ChatResponse.ResponseResultStatus.SUCCESS, chatRoomId, user2.getSessionId()));
        } catch (Exception e) {
            logger.warn("Exception occurs while checking waiting users", e);
        }
    }


}
