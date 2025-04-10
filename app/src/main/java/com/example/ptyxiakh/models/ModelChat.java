package com.example.ptyxiakh.models;

public class ModelChat {
    private String sender;
    private String receiver;
    private String message;
    private String timestamp;
    private boolean isSeen; // Add this field
    private String type; // Add this field for type of message

    // Default constructor required for calls to DataSnapshot.getValue(ModelChat.class)
    public ModelChat() {
    }

    public ModelChat(String sender, String receiver, String message, String timestamp, boolean isSeen, String type) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.timestamp = timestamp;
        this.isSeen = isSeen;
        this.type = type;
    }

    // Getters and setters
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public String getType() { // Add this method
        return type;
    }

    public void setType(String type) { // Add this method
        this.type = type;
    }
}
