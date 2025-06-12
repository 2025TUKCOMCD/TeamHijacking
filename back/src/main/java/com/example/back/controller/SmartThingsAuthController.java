package com.example.back.controller;

import com.example.back.domain.SmartThingsToken;
import com.example.back.repository.SmartThingsTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException; // WebClient 에러 핸들링을 위해 추가

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional; // Optional 임포트 추가

@Controller
@RequestMapping("/callback")
@Slf4j
public class SmartThingsAuthController {

    private final WebClient webClient;
    private final SmartThingsTokenRepository smartThingsTokenRepository; // final로 선언하고 생성자 주입

    @Value("${smartthings.client-id}")
    private String clientId;

    @Value("${smartthings.client-secret}")
    private String clientSecret;

    @Value("${smartthings.redirect-uri}")
    private String redirectUri;

    @Value("${smartthings.token-url}")
    private String tokenUrl;

    // WebClient와 SmartThingsTokenRepository를 생성자를 통해 주입받습니다.
    public SmartThingsAuthController(WebClient webClient, SmartThingsTokenRepository smartThingsTokenRepository) {
        this.webClient = webClient;
        this.smartThingsTokenRepository = smartThingsTokenRepository;
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
    @GetMapping
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
            log.error(errorMessage);
            model.addAttribute("success", false);
            model.addAttribute("message", errorMessage);
            //model.addAttribute("androidDeepLink", buildAndroidDeepLink(false, errorMessage)); // SmartThings 토큰, 백엔드 세션 토큰 미포함
            return "auth-result"; // 실패 HTML 템플릿 반환
        }

        // state 값에서 userId 추출 (형식: userId_csrfToken)
        String userId = "test"; // 초기화
        if (state != null && state.contains("_")) {
            String[] parts = state.split("_", 2);
            userId = parts[0];
            // csrfToken = parts[1]; // CSRF 토큰은 여기서는 사용하지 않음
        }

        // 필수 파라미터 유효성 검사 (code와 userId 모두 필수)
        if (code == null /*|| userId == null || userId.isEmpty()*/) { // userId가 null이거나 빈 문자열인 경우도 처리
            String errorMessage = "SmartThings 연동에 필요한 정보(code 또는 userId)가 부족합니다. userId: " + userId;
            log.error(errorMessage);
            model.addAttribute("success", false);
            model.addAttribute("message", errorMessage);
            // model.addAttribute("androidDeepLink", buildAndroidDeepLink(false, errorMessage)); // SmartThings 토큰, 백엔드 세션 토큰 미포함
            return "auth-result";
        }

        try {
            log.info("Attempting to exchange code for tokens with SmartThings for userId: {}", userId);
            log.debug("Client ID: {}, Redirect URI: {}, Code: {}", clientId, redirectUri, code); // clientSecret은 로그에 직접 출력하지 않음

            // Basic Auth 헤더 생성 (client_id:client_secret을 Base64 인코딩)
            String credentials = clientId + ":" + clientSecret;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + encodedCredentials;

            // SmartThings 토큰 발급 API로 POST 요청 수행
            Map<String, Object> tokenResponse = webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                            .with("client_id", clientId) // SmartThings API 명세에 따라 body에도 client_id 포함
                            .with("code", code)
                            .with("redirect_uri", redirectUri))
                    .retrieve()
                    // 응답 코드가 2xx (성공) 범위가 아니면 onErrorStatus로 예외 처리
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response ->
                            response.bodyToMono(String.class)
                                    .map(body -> {
                                        log.error("SmartThings token request failed with status {}. Response body: {}", response.statusCode(), body);
                                        return new WebClientResponseException(response.statusCode().value(), response.statusCode().toString(), null, body.getBytes(), null);
                                    })
                    )
                    .bodyToMono(Map.class)
                    .block(); // Mono<Map<String, Object>>를 기다려 Map으로 변환

            log.info("SmartThings token exchange response received for userId: {}", userId);
            log.debug("Token response: {}", tokenResponse); // 전체 응답 로그 (민감 정보 주의)

            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                String errorMessage = "SmartThings token exchange failed. No access_token in response: " + tokenResponse;
                log.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }

            String accessToken = (String) tokenResponse.get("access_token");
            String refreshToken = (String) tokenResponse.get("refresh_token");
            // expires_in은 보통 long 타입으로 오므로, Number로 받은 후 longValue()로 변환
            Long expiresIn = ((Number) tokenResponse.get("expires_in")).longValue();
            String scope = (String) tokenResponse.get("scope");

            // --- 데이터베이스에 토큰 저장 또는 업데이트 ---
            // userId로 기존 토큰을 찾아보고, 없으면 새로운 SmartThingsToken 객체를 생성합니다.
            SmartThingsToken smartThingsToken = smartThingsTokenRepository.findByUserId(userId)
                    .orElseGet(SmartThingsToken::new); // 기존 토큰 없으면 새로 생성

            // 토큰 정보 업데이트
            smartThingsToken.setAccessToken(accessToken);
            smartThingsToken.setRefreshToken(refreshToken);
            smartThingsToken.setExpiresIn(expiresIn);
            smartThingsToken.setScope(scope);
            smartThingsToken.setUserId(userId); // userId는 필수적으로 설정
            smartThingsToken.setCreatedAt(LocalDateTime.now());
            smartThingsToken.calculateExpiresAt(); // 만료 시간 계산 로직

            smartThingsTokenRepository.save(smartThingsToken);
            log.info("SmartThings tokens saved/updated successfully for userId: {}", userId);

            // --- 웹 페이지 및 딥링크 응답 구성 ---
            model.addAttribute("success", true);
            model.addAttribute("message", "SmartThings 연동이 성공적으로 완료되었습니다!");
            // 클라이언트 앱에는 SmartThings 토큰을 직접 보내지 않음 (보안 강화)
            //model.addAttribute("androidDeepLink", buildAndroidDeepLink(true, model.getAttribute("message").toString()));

            return "auth-result"; // 성공 HTML 템플릿 반환
        } catch (WebClientResponseException e) {
            String errorMessage = "SmartThings API 오류 (WebClient): Status " + e.getStatusCode() + ", Body: " + e.getResponseBodyAsString();
            log.error(errorMessage, e); // 예외 상세 로깅
            model.addAttribute("success", false);
            model.addAttribute("message", "SmartThings 연동 중 API 오류 발생: " + e.getResponseBodyAsString());
            //model.addAttribute("androidDeepLink", buildAndroidDeepLink(false, model.getAttribute("message").toString()));
            return "auth-result";
        } catch (Exception e) {
            String errorMessage = "예상치 못한 오류 발생 (SmartThings 연동): " + e.getMessage();
            log.error(errorMessage, e); // 예외 상세 로깅
            model.addAttribute("success", false);
            model.addAttribute("message", errorMessage);
            //model.addAttribute("androidDeepLink", buildAndroidDeepLink(false, errorMessage));
            return "auth-result";
        }
    }

    /**
     * 안드로이드 딥 링크를 생성하는 유틸리티 메서드.
     * 이 메서드는 SmartThings 토큰이나 백엔드 세션 토큰을 직접 딥링크로 전달하지 않습니다.
     *
     * @param success 연동 성공 여부
     * @param message 사용자에게 보여줄 메시지
     * @return 앱으로 돌아갈 딥링크 URL
     */
//    private String buildAndroidDeepLink(boolean success, String message) {
//        String androidScheme = "mysmartthingsapp"; // 실제 앱의 스킴으로 변경하세요 (예: myapp)
//        String androidHost = "auth"; // 실제 앱의 호스트로 변경하세요 (예: result)
//
//        StringBuilder deepLinkBuilder = new StringBuilder();
//        deepLinkBuilder.append(androidScheme).append("://").append(androidHost);
//        deepLinkBuilder.append("?status=").append(success ? "success" : "failure");
//        deepLinkBuilder.append("&message=").append(URLEncoder.encode(message, StandardCharsets.UTF_8));
//
//        // SmartThings 토큰이나 백엔드 세션 토큰은 딥링크로 전달하지 않음
//        // 앱은 이 성공/실패 메시지를 받고, 필요 시 서버의 다른 API를 호출하여 상태를 확인할 수 있습니다.
//        return deepLinkBuilder.toString();
//    }
}