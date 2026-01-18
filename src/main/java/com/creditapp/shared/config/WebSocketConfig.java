package com.creditapp.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.creditapp.bank.websocket.ApplicationQueueWebSocketHandler;
import com.creditapp.bank.websocket.ApplicationQueueWebSocketInterceptor;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ApplicationQueueWebSocketHandler applicationQueueWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(applicationQueueWebSocketHandler, "/ws/bank/{bankId}/applications")
            .addInterceptors(new ApplicationQueueWebSocketInterceptor())
            .setAllowedOrigins("*")
            .withSockJS();
    }
}
