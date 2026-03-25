package com.anderson.msnotification.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
public class NotificationLog {

    private String id;
    private String customerId;
    private String message;
    private LocalDateTime sentAt;
    private boolean delivered;

    public NotificationLog() {
    }
    public NotificationLog(String id, String customerId, String message,
                           LocalDateTime sentAt, boolean delivered) {
        this.id = id;
        this.customerId = customerId;
        this.message = message;
        this.sentAt = sentAt;
        this.delivered = delivered;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }
}
