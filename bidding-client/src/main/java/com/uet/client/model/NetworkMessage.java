package com.uet.client.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public abstract class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    protected LocalDateTime timestamp;
    protected String actionType;

    public NetworkMessage(String actionType) {
        this.actionType = actionType;
        this.timestamp = LocalDateTime.now();
    }

    public String getActionType() { return actionType; }
    public LocalDateTime getTimestamp() { return timestamp; }
}