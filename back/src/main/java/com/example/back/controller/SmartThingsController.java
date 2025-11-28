package com.example.back.controller;

import com.example.back.domain.SmartThingsToken;
import com.example.back.service.SmartThingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/smartthings")
public class SmartThingsController {

    @Autowired
    private SmartThingsService smartThingsService;

    @GetMapping
    public ResponseEntity<?> getSmartThingsAccessToken(@RequestParam String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "사용자 ID(userId)는 필수입니다."));
        }

        try {
            // SmartThingsApiService를 통해 유효한(또는 갱신된) 토큰 객체를 가져옵니다.
            SmartThingsToken validToken = smartThingsService.getValidSmartThingsToken(userId);

            // 클라이언트 앱에 Access Token만 전달합니다.
            Map<String, String> response = new HashMap<>();
            response.put("accessToken", validToken.getAccessToken());
            // 필요하다면 만료 시간을 함께 전달하여 앱이 미리 알 수 있도록 할 수 있습니다.
            response.put("expiresAt", validToken.getExpiresAt().toString());
            response.put("tokenType", "Bearer"); // SmartThings는 Bearer 토큰을 사용합니다.

            System.out.println("사용자 ID: " + userId + "에게 유효한 SmartThings Access Token을 제공합니다.");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.err.println("사용자 ID: " + userId + "의 SmartThings Access Token 조회/갱신 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "SmartThings 토큰을 가져오는 데 실패했습니다: " + e.getMessage()));
        }
    }

}
