package com.example.back.service;


import com.example.back.domain.AccessToken;
import com.example.back.domain.AuthorizationCode;
import com.example.back.dto.kakao.AccessTokenResponse;
import com.example.back.dto.kakao.KakaoTokenResponse;
import com.example.back.dto.kakao.KakaoUserInfoResponse;
import com.example.back.repository.AccessTokenRepository;
import com.example.back.repository.AuthorizationCodeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList; // ArrayList import 추가
import java.util.List;
import java.util.Optional; // Optional import 추가
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OAuthService {

    private final AuthorizationCodeRepository authorizationCodeRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final WebClient webClient; // 카카오 API 호출을 위한 WebClient

    @Value("${kakao.client-id}") // application.properties 또는 .env에서 주입
    private String kakaoClientId;

    @Value("${kakao.client-secret}") // application.properties 또는 .env에서 주입
    private String kakaoClientSecret;

    @Value("${kakao.redirect-uri}") // 우리 서버의 카카오 콜백 URI
    private String kakaoRedirectUri;

    @Value("${smartthings.token.expires-in}") // SmartThings 토큰 만료 시간 (초)
    private int smartthingsTokenExpiresIn;

    @Value("${smartthings.refresh-token.expires-in}") // SmartThings 리프레시 토큰 만료 시간 (초)
    private int smartthingsRefreshTokenExpiresIn;

    public OAuthService(AuthorizationCodeRepository authorizationCodeRepository,
                        AccessTokenRepository accessTokenRepository,
                        WebClient.Builder webClientBuilder) {
        this.authorizationCodeRepository = authorizationCodeRepository;
        this.accessTokenRepository = accessTokenRepository;
        this.webClient = webClientBuilder.baseUrl("https://kauth.kakao.com").build(); // 카카오 인증 서버
    }

    /**
     * 카카오 인가 코드를 받아 액세스 토큰 및 사용자 정보 요청
     * @param code 카카오 인가 코드
     * @return 카카오 사용자 ID
     */
    public String getKakaoUserId(String code) {
        // 1. 카카오 토큰 요청
        KakaoTokenResponse tokenResponse = webClient.post()
                .uri("/oauth/token")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData(buildKakaoTokenRequest(code)))
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class)
                .block(); // 비동기이지만 간단한 예시를 위해 block 사용. 실제 서비스에서는 Mono/Flux 사용 권장

        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            throw new RuntimeException("Failed to get Kakao access token.");
        }

        // 2. 카카오 사용자 정보 요청
        WebClient userInfoWebClient = WebClient.builder().baseUrl("https://kapi.kakao.com").build(); // 카카오 API 서버
        KakaoUserInfoResponse userInfo = userInfoWebClient.get()
                .uri("/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponse.getAccessToken())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .retrieve()
                .bodyToMono(KakaoUserInfoResponse.class)
                .block();

        if (userInfo == null || userInfo.getId() == null) {
            throw new RuntimeException("Failed to get Kakao user info.");
        }

        return String.valueOf(userInfo.getId()); // 카카오 사용자 고유 ID 반환
    }

    private MultiValueMap<String, String> buildKakaoTokenRequest(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", kakaoClientId);
        formData.add("redirect_uri", kakaoRedirectUri);
        formData.add("code", code);
        formData.add("client_secret", kakaoClientSecret); // client_secret 추가
        return formData;
    }

    /**
     * SmartThings 인가 코드를 생성하고 저장
     * @param clientId SmartThings 클라이언트 ID
     * @param redirectUri SmartThings 리디렉션 URI
     * @param userId 카카오 사용자 ID (우리 서비스의 사용자 ID)
     * @param scopes 요청된 권한 범위
     * @return 생성된 인가 코드
     */
    @Transactional
    public String createAuthorizationCode(String clientId, String redirectUri, String userId, String scopes) {
        // 기존에 사용되지 않은 유효한 코드가 있는지 확인 (선택 사항)
        // for (AuthorizationCode ac : authorizationCodeRepository.findByUserIdAndClientId(userId, clientId)) {
        //     if (!ac.isUsed() && ac.getExpiresAt().isAfter(LocalDateTime.now())) {
        //         return ac.getCode(); // 기존 코드 재사용
        //     }
        // }

        String code = UUID.randomUUID().toString(); // 고유한 인가 코드 생성
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5); // 5분 유효 (SmartThings OAuth 표준)

        AuthorizationCode authCode = new AuthorizationCode();
        authCode.setCode(code);
        authCode.setClientId(clientId);
        authCode.setRedirectUri(redirectUri);
        authCode.setUserId(userId);
        authCode.setScopes(scopes);
        authCode.setExpiresAt(expiresAt);
        authCode.setUsed(false); // 초기에는 사용되지 않음

        authorizationCodeRepository.save(authCode);
        return code;
    }

    /**
     * 인가 코드를 사용하여 액세스 토큰 및 리프레시 토큰 발급
     * @param code SmartThings가 제공한 인가 코드
     * @param clientId SmartThings 클라이언트 ID
     * @param redirectUri SmartThings 리디렉션 URI (인가 코드 발급 시와 일치해야 함)
     * @return 발급된 액세스 토큰 응답 DTO
     */
    @Transactional
    public AccessTokenResponse issueTokens(String code, String clientId, String redirectUri) {
        AuthorizationCode authCode = authorizationCodeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid authorization code."));

        // 인가 코드 유효성 검증
        if (authCode.isUsed() || authCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Authorization code is expired or already used.");
        }
        if (!authCode.getClientId().equals(clientId) || !authCode.getRedirectUri().equals(redirectUri)) {
            throw new IllegalArgumentException("Client ID or Redirect URI does not match.");
        }

        // 인가 코드 사용 처리
        authCode.setUsed(true);
        authorizationCodeRepository.save(authCode);

        // 기존에 발급된 동일한 client_id, user_id, token_type의 액세스/리프레시 토큰이 있다면 무효화 또는 갱신 처리 (선택 사항)
        List<AccessToken> existingTokens = new ArrayList<>(); // List를 먼저 초기화합니다.

        // access_token 조회 및 추가
        accessTokenRepository.findByUserIdAndClientIdAndTokenType(authCode.getUserId(), clientId, "access_token")
                .ifPresent(existingTokens::add); // Optional에 값이 있으면 리스트에 추가합니다.

        // refresh_token 조회 및 추가
        accessTokenRepository.findByUserIdAndClientIdAndTokenType(authCode.getUserId(), clientId, "refresh_token")
                .ifPresent(existingTokens::add); // Optional에 값이 있으면 리스트에 추가합니다.

        for (AccessToken token : existingTokens) {
            token.setExpiresAt(LocalDateTime.now().minusSeconds(1)); // 즉시 만료 처리
            accessTokenRepository.save(token);
        }


        // 액세스 토큰 생성
        String newAccessTokenValue = UUID.randomUUID().toString();
        LocalDateTime accessTokenExpiresAt = LocalDateTime.now().plusSeconds(smartthingsTokenExpiresIn);

        AccessToken newAccessToken = new AccessToken();
        newAccessToken.setToken(newAccessTokenValue);
        newAccessToken.setTokenType("access_token");
        newAccessToken.setUserId(authCode.getUserId());
        newAccessToken.setClientId(clientId);
        newAccessToken.setScopes(authCode.getScopes());
        newAccessToken.setExpiresAt(accessTokenExpiresAt);

        // 리프레시 토큰 생성 (필요한 경우)
        String newRefreshTokenValue = UUID.randomUUID().toString();
        LocalDateTime refreshTokenExpiresAt = LocalDateTime.now().plusSeconds(smartthingsRefreshTokenExpiresIn);

        AccessToken newRefreshToken = new AccessToken();
        newRefreshToken.setToken(newRefreshTokenValue);
        newRefreshToken.setTokenType("refresh_token");
        newRefreshToken.setUserId(authCode.getUserId());
        newRefreshToken.setClientId(clientId);
        newRefreshToken.setScopes(authCode.getScopes()); // 리프레시 토큰도 동일 스코프
        newRefreshToken.setExpiresAt(refreshTokenExpiresAt);
        newRefreshToken.setRefreshTokenId(null); // 리프레시 토큰은 자신의 refresh_token_id가 없음

        newAccessToken.setRefreshTokenId(newRefreshTokenValue); // 액세스 토큰이 어떤 리프레시 토큰에 의해 발급되었는지 기록

        accessTokenRepository.save(newAccessToken);
        accessTokenRepository.save(newRefreshToken);

        return new AccessTokenResponse(
                newAccessTokenValue,
                "Bearer",
                smartthingsTokenExpiresIn,
                newRefreshTokenValue,
                authCode.getScopes()
        );
    }

    /**
     * 리프레시 토큰을 사용하여 액세스 토큰 갱신
     * @param refreshTokenValue 기존 리프레시 토큰
     * @param clientId SmartThings 클라이언트 ID
     * @return 갱신된 액세스 토큰 응답 DTO
     */
    @Transactional
    public AccessTokenResponse refreshAccessToken(String refreshTokenValue, String clientId) {
        AccessToken refreshToken = accessTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token."));

        // 리프레시 토큰 유효성 검증
        if (!refreshToken.getTokenType().equals("refresh_token") || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Refresh token is invalid or expired.");
        }
        if (!refreshToken.getClientId().equals(clientId)) {
            throw new IllegalArgumentException("Client ID does not match the refresh token.");
        }

        // 기존 액세스 토큰 무효화 (선택 사항이지만 권장)
        // 리프레시 토큰 ID가 현재 리프레시 토큰인 모든 액세스 토큰을 찾아서 만료 처리
        List<AccessToken> associatedAccessTokens = accessTokenRepository.findByRefreshTokenId(refreshTokenValue)
                .stream()
                .filter(token -> token.getTokenType().equals("access_token") && token.getExpiresAt().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());

        for (AccessToken oldAccessToken : associatedAccessTokens) {
            oldAccessToken.setExpiresAt(LocalDateTime.now().minusSeconds(1)); // 즉시 만료 처리
            accessTokenRepository.save(oldAccessToken);
        }

        // 새 액세스 토큰 생성
        String newAccessTokenValue = UUID.randomUUID().toString();
        LocalDateTime accessTokenExpiresAt = LocalDateTime.now().plusSeconds(smartthingsTokenExpiresIn);

        AccessToken newAccessToken = new AccessToken();
        newAccessToken.setToken(newAccessTokenValue);
        newAccessToken.setTokenType("access_token");
        newAccessToken.setUserId(refreshToken.getUserId());
        newAccessToken.setClientId(clientId);
        newAccessToken.setScopes(refreshToken.getScopes());
        newAccessToken.setExpiresAt(accessTokenExpiresAt);
        newAccessToken.setRefreshTokenId(refreshTokenValue); // 새 액세스 토큰이 이 리프레시 토큰에 의해 발급되었음을 기록

        accessTokenRepository.save(newAccessToken);

        return new AccessTokenResponse(
                newAccessTokenValue,
                "Bearer",
                smartthingsTokenExpiresIn,
                refreshTokenValue, // 리프레시 토큰은 그대로 유지 (또는 갱신 정책에 따라 새로운 리프레시 토큰 발급)
                refreshToken.getScopes()
        );
    }
}