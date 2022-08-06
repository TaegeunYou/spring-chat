package chat.project.config;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@EnableAsync
public class AsyncConfiguration {

    public Executor asyncThreadPool() {

        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

        taskExecutor.setCorePoolSize(5);        //1. 맨 처음에 5개로 시작해서
        taskExecutor.setMaxPoolSize(10);        //3. 10개로 늘린다
        taskExecutor.setQueueCapacity(100);     //2. 대기 가능한 Queue의 수 보다 많아지면
        taskExecutor.setThreadNamePrefix("Async-Executor-");
        taskExecutor.setDaemon(true);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());    //예외와 누락없이 최대한 처리하기 위한 방법
        taskExecutor.initialize();

        return taskExecutor;
    }
}
