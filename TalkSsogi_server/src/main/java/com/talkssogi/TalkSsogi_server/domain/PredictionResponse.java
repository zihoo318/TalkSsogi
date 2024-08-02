package com.talkssogi.TalkSsogi_server.domain;

public class PredictionResponse {
    private String sender;

    public PredictionResponse(String sender) {
        this.sender = sender;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}