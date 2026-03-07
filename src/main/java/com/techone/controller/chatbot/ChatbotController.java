package com.techone.controller.chatbot;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/chat")
public class ChatbotController {

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> handleChat(@RequestBody Map<String, String> request) {
        String n8nWebhookUrl = "https://automation.tino.page/webhook/java-springboot";
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(n8nWebhookUrl, request, Map.class);

            Map body = response.getBody();
            System.out.println(response.getBody());
            String aiResponse = "AI không phản hồi";

            if (body != null) {
                Object output = body.get("output");

                if (output != null) {
                    aiResponse = output.toString();
                } else {
                    // Debug xem n8n trả gì
                    aiResponse = body.toString();
                }
            }

            return ResponseEntity.ok(Map.of("reply", aiResponse));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("reply", "Lỗi kết nối: " + e.getMessage()));
        }
    }
}