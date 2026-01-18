package com.creditapp.bank.websocket;

import com.creditapp.bank.dto.ApplicationQueueItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationQueueWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    
    // Map: bankId -> Set of WebSocketSessions
    private static final Map<String, Set<WebSocketSession>> BANK_CONNECTIONS = 
        new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String bankId = extractBankId(session);
        if (bankId != null) {
            BANK_CONNECTIONS.computeIfAbsent(bankId, k -> ConcurrentHashMap.newKeySet())
                .add(session);
            log.info("[WEBSOCKET] Client connected for bank {}. Active connections: {}",
                bankId, BANK_CONNECTIONS.get(bankId).size());
            
            // Send connection confirmation
            sendMessage(session, createMessage("connected", "Connected to application queue"));
        } else {
            session.close(CloseStatus.BAD_DATA.withReason("Bank ID not found in URL"));
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String bankId = extractBankId(session);
        String payload = message.getPayload();
        log.debug("[WEBSOCKET] Message from bank {}: {}", bankId, payload);
        
        // Handle ping/pong for keep-alive
        if ("ping".equals(payload)) {
            sendMessage(session, createMessage("pong", "pong"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String bankId = extractBankId(session);
        if (bankId != null) {
            Set<WebSocketSession> connections = BANK_CONNECTIONS.get(bankId);
            if (connections != null) {
                connections.remove(session);
                log.info("[WEBSOCKET] Client disconnected from bank {}. Active connections: {}",
                    bankId, connections.size());
                if (connections.isEmpty()) {
                    BANK_CONNECTIONS.remove(bankId);
                }
            }
        }
    }

    /**
     * Broadcast new/updated application to all connected officers for a bank
     */
    public void broadcastApplicationUpdate(String bankId, ApplicationQueueItem item) {
        Set<WebSocketSession> connections = BANK_CONNECTIONS.get(bankId);
        if (connections != null && !connections.isEmpty()) {
            String message = createUpdateMessage("application_update", item);
            connections.forEach(session -> {
                try {
                    if (session.isOpen()) {
                        sendMessage(session, message);
                    }
                } catch (IOException e) {
                    log.error("[WEBSOCKET] Failed to send update to session", e);
                    connections.remove(session);
                }
            });
            log.info("[WEBSOCKET] Broadcasted application update for bank {} to {} clients",
                bankId, connections.size());
        }
    }

    /**
     * Broadcast status change to all connected officers for a bank
     */
    public void broadcastStatusChange(String bankId, String applicationId, String oldStatus, String newStatus) {
        Set<WebSocketSession> connections = BANK_CONNECTIONS.get(bankId);
        if (connections != null && !connections.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("applicationId", applicationId);
            data.put("oldStatus", oldStatus);
            data.put("newStatus", newStatus);
            String message = createCustomMessage("status_changed", data);
            
            connections.forEach(session -> {
                try {
                    if (session.isOpen()) {
                        sendMessage(session, message);
                    }
                } catch (IOException e) {
                    log.error("[WEBSOCKET] Failed to send status change to session", e);
                    connections.remove(session);
                }
            });
            log.info("[WEBSOCKET] Broadcasted status change for app {} to {} clients",
                applicationId, connections.size());
        }
    }

    private String extractBankId(WebSocketSession session) {
        try {
            Map<String, Object> attributes = session.getAttributes();
            return (String) attributes.get("bankId");
        } catch (Exception e) {
            log.error("[WEBSOCKET] Failed to extract bankId from session", e);
            return null;
        }
    }

    private String createMessage(String type, String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", type);
        data.put("message", message);
        data.put("timestamp", System.currentTimeMillis());
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("[WEBSOCKET] Failed to serialize message", e);
            return "{}";
        }
    }

    private String createUpdateMessage(String type, ApplicationQueueItem item) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", type);
        data.put("data", item);
        data.put("timestamp", System.currentTimeMillis());
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("[WEBSOCKET] Failed to serialize update message", e);
            return "{}";
        }
    }

    private String createCustomMessage(String type, Map<String, Object> customData) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", type);
        data.putAll(customData);
        data.put("timestamp", System.currentTimeMillis());
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("[WEBSOCKET] Failed to serialize custom message", e);
            return "{}";
        }
    }

    private void sendMessage(WebSocketSession session, String message) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }
}
