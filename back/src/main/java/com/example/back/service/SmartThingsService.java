package com.example.back.service;

import com.example.back.domain.SmartThingsToken;
import com.example.back.repository.SmartThingsTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SmartThingsService {
    private final SmartThingsTokenRepository smartThingsTokenRepository;
    private final WebClient webClient;

    @Value("${smartthings.client-id}")
    private String clientId;

    @Value("${smartthings.client-secret}")
    private String clientSecret;

    @Value("${smartthings.token-url}")
    private String tokenUrl;

    public SmartThingsToken refreshAccessToken(String refreshToken, String userId) {
        System.out.println("사용자 ID: " + userId + "의 액세스 토큰 갱신을 시도합니다.");
        try {
            // Basic Auth 헤더 생성 (client_id:client_secret을 Base64 인코딩)
            String credentials = clientId + ":" + clientSecret;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + encodedCredentials;

            Map<String, Object> tokenResponse = webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header(HttpHeaders.AUTHORIZATION, authHeader) // <--- Basic Auth 헤더 추가!
                    .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                            .with("client_id", clientId) // client_id는 body에도 포함
                            .with("refresh_token", refreshToken))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response ->
                            response.bodyToMono(String.class)
                                    .map(body -> {
                                        System.err.println("SmartThings 토큰 갱신 실패. 상태: " + response.statusCode() + ", 응답 본문: " + body);
                                        return new WebClientResponseException(response.statusCode().value(), response.statusCode().toString(), null, body.getBytes(), null);
                                    })
                    )
                    .bodyToMono(Map.class)
                    .block();

            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                System.err.println("SmartThings 토큰 갱신 실패. 응답에 access_token이 없습니다: " + tokenResponse);
                throw new RuntimeException("SmartThings 토큰 갱신 실패: 유효하지 않은 응답.");
            }

            String newAccessToken = (String) tokenResponse.get("access_token");
            String newRefreshToken = (String) tokenResponse.getOrDefault("refresh_token", refreshToken);
            Long newExpiresIn = ((Number) tokenResponse.get("expires_in")).longValue();
            String newScope = (String) tokenResponse.get("scope");

            SmartThingsToken smartThingsToken = smartThingsTokenRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("사용자 ID: " + userId + "에 대한 기존 토큰을 찾을 수 없습니다. 토큰을 갱신할 수 없습니다."));

            smartThingsToken.setAccessToken(newAccessToken);
            smartThingsToken.setRefreshToken(newRefreshToken);
            smartThingsToken.setExpiresIn(newExpiresIn);
            smartThingsToken.setScope(newScope);
            smartThingsToken.setCreatedAt(LocalDateTime.now());
            smartThingsToken.calculateExpiresAt();

            SmartThingsToken savedToken = smartThingsTokenRepository.save(smartThingsToken);
            System.out.println("사용자 ID: " + userId + "의 SmartThings 토큰이 성공적으로 갱신되었습니다.");
            return savedToken;

        } catch (WebClientResponseException e) {
            System.err.println("SmartThings 토큰 갱신 중 WebClient 오류 발생. 상태: " + e.getStatusCode() + ", 본문: " + e.getResponseBodyAsString());
            e.printStackTrace();
            throw new RuntimeException("SmartThings API 토큰 갱신 중 오류: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            System.err.println("사용자 ID: " + userId + "의 SmartThings 토큰 갱신 중 예상치 못한 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("토큰 갱신 중 내부 서버 오류: " + e.getMessage(), e);
        }
    }
    public SmartThingsToken getValidSmartThingsToken(String userId) {
        // 1. DB에서 사용자 토큰 조회
        SmartThingsToken smartThingsToken = smartThingsTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("SmartThings 연동되지 않은 사용자입니다: " + userId));

        // 2. Access Token 만료 여부 확인 및 갱신 조건 검사
        // 만료 시간이 현재 시간보다 이전이거나, 현재 시간으로부터 5분 이내로 만료될 경우 갱신 시도
        // smartThingsToken.getExpiresAt()이 null인 경우 (예: 초기 데이터 문제)도 갱신 필요하다고 판단
        if (smartThingsToken.getExpiresAt() == null || smartThingsToken.getExpiresAt().isBefore(LocalDateTime.now().plusMinutes(5))) {
            System.out.println("사용자 ID: " + userId + "의 액세스 토큰이 만료되었거나 만료가 임박했습니다. 갱신을 시도합니다...");
            // refreshAccessToken 메서드를 호출하여 토큰을 갱신하고, 갱신된 토큰 객체를 반환
            return refreshAccessToken(smartThingsToken.getRefreshToken(), userId);
        }

        System.out.println("사용자 ID: " + userId + "의 유효한 액세스 토큰을 사용합니다.");
        return smartThingsToken; // 기존의 유효한 토큰 반환
    }
}
