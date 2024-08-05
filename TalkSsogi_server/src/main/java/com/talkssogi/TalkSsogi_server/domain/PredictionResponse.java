// domain/PredictionResponse.java
package com.talkssogi.TalkSsogi_server.domain;

public class PredictResponse {
    private String sender;

    public PredictResponse(String sender) {
        this.sender = sender;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
