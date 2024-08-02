package com.talkssogi.TalkSsogi_server.controller;

import com.talkssogi.TalkSsogi_server.domain.PredictionResponse;
import com.talkssogi.TalkSsogi_server.domain.PredictionRequest;
import com.talkssogi.TalkSsogi_server.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class Page10Controller {

    @Autowired
    private PredictionService predictionService;

    @PostMapping("/predict")
    public PredictionResponse predictSender(@RequestBody PredictionRequest request) {
        String sender = predictionService.predictSender(request.getQuery());
        //클라이언트에게 예측 결과를 전달
        return new PredictionResponse(sender);
    }
}