package com.talkssogi.TalkSsogi_server.service;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class PredictionService {

    public String predictSender(String query) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python3", "path/to/your/python/script.py", query);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            process.waitFor();
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }
}