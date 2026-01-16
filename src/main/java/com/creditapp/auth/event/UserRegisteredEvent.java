package com.creditapp.auth.event;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

public class UserRegisteredEvent extends ApplicationEvent {
    private final UUID userId;
    private final String email;
    private final String userName;

    public UserRegisteredEvent(UUID userId, String email, String userName) {
        super(new Object());
        this.userId = userId;
        this.email = email;
        this.userName = userName;
    }

    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getUserName() { return userName; }
}