package com.kayz.heac.gateway;

import io.github.pigmesh.ai.deepseek.core.DeepSeekClient;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class AIController {
    @Autowired
    private DeepSeekClient deepSeekClient;

    // sse 流式返回
    @GetMapping(value = "/chat")
    public Flux<ChatCompletionResponse> chat(String prompt) {
        return deepSeekClient.chatFluxCompletion(prompt);
    }
}
