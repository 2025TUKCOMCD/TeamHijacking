package com.example.back.controller;


import com.example.back.service.SmartThingsAuthService;
import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j; // <-- 이 어노테이션을 제거하세요.
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
// @Slf4j // <-- 이 어노테이션을 제거하세요.
public class SmartThingsAuthController {

    private final SmartThingsAuthService smartThingsAuthService;


    @PostMapping("/smartthings-callback")
    public ResponseEntity<Map<String, String>> handleSmartThingsCode(@RequestBody Map<String, String> payload) {
        String code = payload.get("code");
        String state = payload.get("state");
        String userId = payload.get("userId");

        if (code == null || userId == null || userId.trim().isEmpty()) {
            System.err.println("Missing authorization code or userId in SmartThings callback payload. Code: " + code + ", UserId: " + userId); // log.warn 대신 System.err.println
            return ResponseEntity.badRequest().body(Map.of(
                    "success", "false",
                    "message", "인증 코드 또는 사용자 ID가 누락되었습니다."
            ));
        }

        try {
            smartThingsAuthService.exchangeCodeForTokens(code, state, userId);
            System.out.println("Successfully exchanged code and saved tokens for user: " + userId); // log.info 대신 System.out.println
            return ResponseEntity.ok(Map.of(
                    "success", "true",
                    "message", "SmartThings 연동에 성공했습니다!"
            ));
        } catch (Exception e) {
            System.err.println("Error processing SmartThings authentication for user " + userId + ": " + e.getMessage()); // log.error 대신 System.err.println
            e.printStackTrace(); // 스택 트레이스 출력
            return ResponseEntity.status(500).body(Map.of(
                    "success", "false",
                    "message", "SmartThings 연동 처리 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
}