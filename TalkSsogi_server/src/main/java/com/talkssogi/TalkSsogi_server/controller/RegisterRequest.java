package com.talkssogi.TalkSsogi_server.controller;

public class RegisterRequest {
    private String userId;

    public RegisterRequest() {
    }

    public RegisterRequest(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
