package com.creditapp.bank.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
public class ApplicationQueueWebSocketInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        try {
            String path = request.getURI().getPath();
            // Extract bankId from URL: /ws/bank/{bankId}/applications
            String[] pathParts = path.split("/");
            if (pathParts.length >= 4 && "ws".equals(pathParts[1]) && "bank".equals(pathParts[2])) {
                String bankId = pathParts[3];
                attributes.put("bankId", bankId);
                log.debug("[WEBSOCKET_HANDSHAKE] Extracted bankId: {} from path: {}", bankId, path);
                return true;
            } else {
                log.warn("[WEBSOCKET_HANDSHAKE] Invalid URL format: {}", path);
                return false;
            }
        } catch (Exception e) {
            log.error("[WEBSOCKET_HANDSHAKE] Error extracting bankId", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No-op after handshake
    }
}
