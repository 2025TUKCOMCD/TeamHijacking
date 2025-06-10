package com.example.back.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value; // application.properties 값 주입용
import org.springframework.stereotype.Controller; // @RestController 대신 @Controller 사용 (매우 중요!)
import org.springframework.ui.Model; // HTML 템플릿에 데이터 전달용
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.BodyInserters; // POST 요청 바디 구성용
import org.springframework.web.reactive.function.client.WebClient; // HTTP 클라이언트
import org.springframework.http.MediaType; // Content-Type 설정용

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map; // 토큰 응답 파싱용

@Controller // 뷰 (HTML)를 반환할 것이므로 @Controller를 사용해야 합니다.
@RequestMapping("/callback") // SmartThings redirect_uri와 일치해야 합니다. (예: https://seemore.io.kr/callback)
@Slf4j // Lombok 로깅 어노테이션 (필요 시 Lombok 설정 확인)
public class SmartThingsAuthController {

    private final WebClient webClient; // WebClient 주입

    // application.properties에서 설정 값을 주입받습니다.
    @Value("${smartthings.client-id}")
    private String clientId;

    @Value("${smartthings.client-secret}")
    private String clientSecret;

    @Value("${smartthings.redirect-uri}")
    private String redirectUri; // 예: https://seemore.io.kr/callback

    @Value("${smartthings.token-url}")
    private String tokenUrl; // 예: https://api.smartthings.com/oauth/token

    // WebClient는 BackApplication에서 @Bean으로 등록했으므로 여기서 주입받습니다.
    public SmartThingsAuthController(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * SmartThings로부터 authorization code를 GET 요청으로 받아 처리하는 엔드포인트
     * redirect_uri (예: https://seemore.io.kr/callback)
     *
     * @param code SmartThings에서 전달하는 인증 코드
     * @param state SmartThings에서 전달하는 상태 값 (여기에 사용자 ID 포함 권장)
     * @param model Thymeleaf 템플릿에 데이터를 전달하기 위한 Model 객체
     * @return HTML 템플릿 이름 (성공 또는 실패 페이지)
     */
    @GetMapping // 이 @GetMapping은 클래스 레벨의 @RequestMapping("/callback")에 대한 상대 경로를 의미합니다.
    public String handleSmartThingsCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            Model model
    ) {
        log.info("Received SmartThings callback. Code: {}, State: {}", code, state);

        // 1. SmartThings 자체에서 에러가 발생한 경우 (사용자 거부 등)
        if (error != null) {
            String errorMessage = "SmartThings 인증에 실패했습니다: " + (errorDescription != null ? errorDescription : error);
            log.error(errorMessage); // 로그 남기기
            model.addAttribute("success", false);
            model.addAttribute("message", errorMessage);
            model.addAttribute("androidDeepLink", buildAndroidDeepLink(false, errorMessage, null));
            return "auth-result"; // 실패 HTML 템플릿 반환
        }

        // state 값에서 userId 추출 (형식: userId_csrfToken)
        String userId = null;
        if (state != null && state.contains("_")) {
            String[] parts = state.split("_", 2);
            userId = parts[0];
            // csrfToken = parts[1]; // CSRF 토큰은 여기서는 사용하지 않음
        }

        // 필수 파라미터 유효성 검사
        if (code == null || userId == null || userId.trim().isEmpty()) {
            String errorMessage = "SmartThings 연동에 필요한 정보(code 또는 userId)가 부족합니다.";
            log.error(errorMessage);
            model.addAttribute("success", false);
            model.addAttribute("message", errorMessage);
            model.addAttribute("androidDeepLink", buildAndroidDeepLink(false, errorMessage, null));
            return "auth-result";
        }

        try {
            log.info("Attempting to exchange code for tokens with SmartThings. userId: {}", userId);

            // --- SmartThings 토큰 발급 API로 POST 요청을 직접 수행하는 로직 ---
            // 이 부분이 SmartThingsAuthService에 있던 역할입니다.
            Map<String, Object> tokenResponse = webClient.post()
                    .uri(tokenUrl) // SmartThings 토큰 발급 URL (application.properties에서 주입)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED) // 요청 바디 타입 설정
                    .body(BodyInserters.fromFormData("grant_type", "authorization_code") // OAuth 2.0 표준
                            .with("client_id", clientId) // application.properties에서 주입
                            .with("client_secret", clientSecret) // application.properties에서 주입
                            .with("code", code) // SmartThings에서 받은 인증 코드
                            .with("redirect_uri", redirectUri)) // 등록된 redirect_uri와 일치해야 함
                    .retrieve() // 응답 받기
                    .bodyToMono(Map.class) // 응답 바디를 Map 형태로 파싱
                    .block(); // 비동기 작업을 동기적으로 대기 (컨트롤러에서는 간단히 block 사용)

            // 토큰 응답에서 필요한 정보 추출 (null 체크 추가 권장)
            String accessToken = (String) tokenResponse.get("access_token");
            String refreshToken = (String) tokenResponse.get("refresh_token");
            // long expiresIn = ((Number) tokenResponse.get("expires_in")).longValue(); // 필요하다면 사용

            log.info("SmartThings tokens successfully received. Access Token (partial): {}", accessToken != null ? accessToken.substring(0, Math.min(accessToken.length(), 10)) + "..." : "N/A");
            log.info("Refresh Token (partial): {}", refreshToken != null ? refreshToken.substring(0, Math.min(refreshToken.length(), 10)) + "..." : "N/A");

            // TODO:
            // -------------------------------------------------------------------------------------
            // !!!! 중요: 여기서 받은 accessToken과 refreshToken을 userId와 함께 데이터베이스에 저장해야 합니다. !!!!
            // SmartThingsAuthService가 없으므로 이 로직은 당신이 직접 구현해야 합니다.
            // 예시: yourSmartThingsTokenRepository.save(new SmartThingsToken(userId, accessToken, refreshToken));
            // -------------------------------------------------------------------------------------

            model.addAttribute("success", true);
            model.addAttribute("message", "SmartThings 연동이 성공적으로 완료되었습니다!");
            String backendAuthToken = "your_backend_session_token_if_any"; // 실제 백엔드 세션 토큰이 있다면 여기에
            model.addAttribute("androidDeepLink", buildAndroidDeepLink(true, model.getAttribute("message").toString(), backendAuthToken));

            return "auth-result"; // 성공 HTML 템플릿 반환
        } catch (Exception e) {
            String errorMessage = "SmartThings 연동 처리 중 오류가 발생했습니다: " + e.getMessage();
            log.error(errorMessage, e); // 예외 상세 로깅
            model.addAttribute("success", false);
            model.addAttribute("message", errorMessage);
            model.addAttribute("androidDeepLink", buildAndroidDeepLink(false, errorMessage, null));
            return "auth-result"; // 실패 HTML 템플릿 반환
        }
    }

    // 안드로이드 딥 링크 생성 유틸리티 메서드 (이전과 동일)
    private String buildAndroidDeepLink(boolean success, String message, String backendAuthToken) {
        String androidScheme = "mysmartthingsapp";
        String androidHost = "auth";

        StringBuilder deepLinkBuilder = new StringBuilder();
        deepLinkBuilder.append(androidScheme).append("://").append(androidHost);
        deepLinkBuilder.append("?status=").append(success ? "success" : "failure");
        deepLinkBuilder.append("&message=").append(URLEncoder.encode(message, StandardCharsets.UTF_8));

        if (backendAuthToken != null && !backendAuthToken.isEmpty()) {
            deepLinkBuilder.append("&token=").append(URLEncoder.encode(backendAuthToken, StandardCharsets.UTF_8));
        }
        return deepLinkBuilder.toString();
    }
}