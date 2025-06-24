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
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/callback")
@Slf4j
public class SmartThingsAuthController {

    private final WebClient webClient;
    private final SmartThingsTokenRepository smartThingsTokenRepository;

    @Value("${smartthings.client-id}")
    private String clientId;

    @Value("${smartthings.client-secret}")
    private String clientSecret;

    @Value("${smartthings.redirect-uri}")
    private String redirectUri;

    @Value("${smartthings.token-url}")
    private String tokenUrl;

    public SmartThingsAuthController(WebClient webClient, SmartThingsTokenRepository smartThingsTokenRepository) {
        this.webClient = webClient;
        this.smartThingsTokenRepository = smartThingsTokenRepository;
    }

    @GetMapping
    public String handleSmartThingsCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            Model model
    ) {
        log.info("Received SmartThings callback. Code: {}, State: {}", code, state);

        String userId = "";
        if (state != null) {
            userId = state; // userId로 state 값 그대로 사용
        }

        // --- 에러 처리 (state 값 포함하여 딥링크 생성) ---
        if (error != null) {
            String errorMessage = "SmartThings 인증에 실패했습니다: " + (errorDescription != null ? errorDescription : error);
            log.error(errorMessage);
            model.addAttribute("success", false);
            model.addAttribute("message", errorMessage);
            // buildAndroidDeepLink 호출 시 state 값 추가
            model.addAttribute("androidDeepLink", buildAndroidDeepLink(false, errorMessage, userId));
            return "auth-result";
        }

        if (code == null) { // userId는 state에서 추출되므로, code만 필수로 검사
            String errorMessage = "SmartThings 연동에 필요한 정보(code)가 부족합니다. userId: " + userId;
            log.error(errorMessage);
            model.addAttribute("success", false);
            model.addAttribute("message", errorMessage);
            // buildAndroidDeepLink 호출 시 state 값 추가
            model.addAttribute("androidDeepLink", buildAndroidDeepLink(false, errorMessage, userId));
            return "auth-result";
        }

        try {
            log.info("Attempting to exchange code for tokens with SmartThings for userId: {}", userId);
            log.debug("Client ID: {}, Redirect URI: {}, Code: {}", clientId, redirectUri, code);

            String credentials = clientId + ":" + clientSecret;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + encodedCredentials;

            Map<String, Object> tokenResponse = webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                            .with("client_id", clientId)
                            .with("code", code)
                            .with("redirect_uri", redirectUri))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response ->
                            response.bodyToMono(String.class)
                                    .map(body -> {
                                        log.error("SmartThings token request failed with status {}. Response body: {}", response.statusCode(), body);
                                        return new WebClientResponseException(response.statusCode().value(), response.statusCode().toString(), null, body.getBytes(), null);
                                    })
                    )
                    .bodyToMono(Map.class)
                    .block();

            log.info("SmartThings token exchange response received for userId: {}", userId);
            log.debug("Token response: {}", tokenResponse);

            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                String errorMessage = "SmartThings token exchange failed. No access_token in response: " + tokenResponse;
                log.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }

            String accessToken = (String) tokenResponse.get("access_token");
            String refreshToken = (String) tokenResponse.get("refresh_token");
            Long expiresIn = ((Number) tokenResponse.get("expires_in")).longValue();
            String scope = (String) tokenResponse.get("scope");

            SmartThingsToken smartThingsToken = smartThingsTokenRepository.findByUserId(userId)
                    .orElseGet(SmartThingsToken::new);

            smartThingsToken.setAccessToken(accessToken);
            smartThingsToken.setRefreshToken(refreshToken);
            smartThingsToken.setExpiresIn(expiresIn);
            smartThingsToken.setScope(scope);
            smartThingsToken.setUserId(userId);
            smartThingsToken.setCreatedAt(LocalDateTime.now());
            smartThingsToken.calculateExpiresAt();

            smartThingsTokenRepository.save(smartThingsToken);
            log.info("SmartThings tokens saved/updated successfully for userId: {}", userId);

            model.addAttribute("success", true);
            model.addAttribute("message", "SmartThings 연동이 성공적으로 완료되었습니다!");
            // buildAndroidDeepLink 호출 시 state 값 추가
            model.addAttribute("androidDeepLink", buildAndroidDeepLink(true, "SmartThings 연동이 성공적으로 완료되었습니다!", userId));

            return "auth-result";
        } catch (WebClientResponseException e) {
            String errorMessage = "SmartThings API 오류 (WebClient): Status " + e.getStatusCode() + ", Body: " + e.getResponseBodyAsString();
            log.error(errorMessage, e);
            model.addAttribute("success", false);
            model.addAttribute("message", "SmartThings 연동 중 API 오류 발생: " + e.getResponseBodyAsString());
            // buildAndroidDeepLink 호출 시 state 값 추가
            model.addAttribute("androidDeepLink", buildAndroidDeepLink(false, model.getAttribute("message").toString(), userId));
            return "auth-result";
        } catch (Exception e) {
            String errorMessage = "예상치 못한 오류 발생 (SmartThings 연동): " + e.getMessage();
            log.error(errorMessage, e);
            model.addAttribute("success", false);
            model.addAttribute("message", errorMessage);
            // buildAndroidDeepLink 호출 시 state 값 추가
            model.addAttribute("androidDeepLink", buildAndroidDeepLink(false, errorMessage, userId));
            return "auth-result";
        }
    }

    /**
     * 안드로이드 딥 링크를 생성하는 유틸리티 메서드.
     * 이 메서드는 SmartThings 토큰이나 백엔드 세션 토큰을 직접 딥링크로 전달하지 않습니다.
     *
     * @param success 연동 성공 여부
     * @param message 사용자에게 보여줄 메시지
     * @param state SmartThings에서 전달된 원본 state 값 (여기에 userId가 포함됨)
     * @return 앱으로 돌아갈 딥링크 URL
     */
    private String buildAndroidDeepLink(boolean success, String message, String state) {
        String androidScheme = "seemore";
        String androidHost = "main";

        StringBuilder deepLinkBuilder = new StringBuilder();
        deepLinkBuilder.append(androidScheme).append("://").append(androidHost);
        deepLinkBuilder.append("?status=").append(success ? "success" : "failure");
        deepLinkBuilder.append("&message=").append(URLEncoder.encode(message, StandardCharsets.UTF_8));

        if (state != null && !state.isEmpty()) {
            deepLinkBuilder.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
        }

        return deepLinkBuilder.toString();
    }
}