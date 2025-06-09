package com.example.back.controller;


import com.example.back.dto.kakao.AccessTokenResponse;
import com.example.back.service.OAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@Controller // 템플릿 뷰를 반환할 수 있도록 @Controller 사용 (리다이렉션 필요)
@RequestMapping("/oauth")
public class OAuthController {https://enterprise.smartthings.com/locations

    private final OAuthService oAuthService;

    @Value("${smartthings.response-type}") // SmartThings가 요구하는 response_type (코드)
    private String smartthingsResponseType;

    @Value("${smartthings.grant-type.authorization-code}") // SmartThings 인증 코드 부여 타입
    private String smartthingsGrantTypeAuthCode;

    @Value("${smartthings.grant-type.refresh-token}") // SmartThings 리프레시 토큰 부여 타입
    private String smartthingsGrantTypeRefreshToken;

    @Value("${kakao.client-id}") // application.properties의 kakao.client-id 값을 주입
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}") // application.properties의 kakao.redirect-uri 값을 주입
    private String kakaoRedirectUri;
    public OAuthController(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    /**
     * SmartThings 인가 요청 처리 (Step 1: Authorization Code 요청)
     * 사용자를 카카오 로그인 페이지로 리다이렉트
     * GET /oauth/authorize
     */
    @GetMapping("/authorize")
    public String authorize(
            @RequestParam("response_type") String responseType,
            @RequestParam("client_id") String clientId,
            @RequestParam("scope") String scope,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam(value = "state", required = false) String state
    ) {
        // SmartThings의 요청 유효성 검사 (client_id, redirect_uri 등)
        if (!responseType.equals(smartthingsResponseType)) { // "code"
            throw new IllegalArgumentException("Unsupported response_type: " + responseType);
        }
        // TODO: 실제 서비스에서는 등록된 client_id와 redirect_uri인지 검증 필요

        // 사용자 인증을 위해 카카오 로그인 페이지로 리다이렉트
        // 카카오 로그인 성공 후, 우리 서버의 /oauth/kakao/callback 엔드포인트로 리다이렉트됩니다.
        // 이때, SmartThings에서 받은 client_id, redirect_uri, scope, state 값을 함께 전달하여
        // 카카오 콜백 이후 SmartThings 인가 코드를 생성할 때 사용합니다.
        String kakaoAuthUrl = UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", "${kakao.client-id}") // application.properties 또는 .env에서 직접 사용
                .queryParam("redirect_uri", "${kakao.redirect-uri}") // application.properties 또는 .env에서 직접 사용
                .queryParam("scope", "profile_nickname,profile_image") // 카카오에서 요청할 스코프 (이메일 등 추가 가능)
                .queryParam("state", String.join("|", clientId, redirectUri, scope, state != null ? state : "")) // SmartThings 정보를 state에 함께 전달
                .build()
                .toUriString();

        return "redirect:" + kakaoAuthUrl;
    }

    /**
     * 카카오 로그인 콜백 처리 (우리 서버의 중간 단계)
     * 카카오 인가 코드를 받아 카카오 사용자 ID 획득 후, SmartThings 인가 코드를 생성하여 SmartThings에 리다이렉트
     * GET /oauth/kakao/callback
     */
    @GetMapping("/kakao/callback")
    public String kakaoCallback(
            @RequestParam("code") String kakaoCode,
            @RequestParam("state") String smartthingsStateData // SmartThings에서 받은 state 정보 (clientId, redirectUri 등)
    ) {
        String userId = oAuthService.getKakaoUserId(kakaoCode);

        // SmartThings State 데이터 파싱
        String[] stateParts = smartthingsStateData.split("\\|", 4);
        String clientId = stateParts[0];
        String redirectUri = stateParts[1];
        String smartthingsScope = stateParts[2];
        String originalSmartthingsState = stateParts.length > 3 ? stateParts[3] : null;

        // SmartThings 인가 코드 생성 및 저장
        String smartthingsAuthCode = oAuthService.createAuthorizationCode(
                clientId,
                redirectUri,
                userId, // 카카오 사용자 ID를 우리 서비스의 사용자 ID로 사용
                smartthingsScope
        );

        // SmartThings에 인가 코드 및 originalState와 함께 리다이렉트
        String smartthingsRedirectUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("code", smartthingsAuthCode)
                .queryParam("state", originalSmartthingsState) // SmartThings가 보낸 원래 state 값을 그대로 돌려줌
                .build()
                .toUriString();

        return "redirect:" + smartthingsRedirectUrl;
    }

    /**
     * SmartThings 토큰 요청 처리 (Step 2: Token 요청)
     * POST /oauth/token
     */
    @PostMapping("/token")
    @ResponseBody // JSON 응답을 위해 사용
    public ResponseEntity<AccessTokenResponse> token(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam(value = "code", required = false) String code, // 인가 코드 부여 타입일 경우
            @RequestParam(value = "redirect_uri", required = false) String redirectUri, // 인가 코드 부여 타입일 경우
            @RequestParam(value = "refresh_token", required = false) String refreshToken // 리프레시 토큰 부여 타입일 경우
    ) {
        AccessTokenResponse response;

        if (smartthingsGrantTypeAuthCode.equals(grantType)) { // "authorization_code"
            if (code == null || redirectUri == null) {
                throw new IllegalArgumentException("Code and redirect_uri are required for authorization_code grant type.");
            }
            response = oAuthService.issueTokens(code, clientId, redirectUri);
        } else if (smartthingsGrantTypeRefreshToken.equals(grantType)) { // "refresh_token"
            if (refreshToken == null) {
                throw new IllegalArgumentException("Refresh token is required for refresh_token grant type.");
            }
            response = oAuthService.refreshAccessToken(refreshToken, clientId);
        } else {
            throw new IllegalArgumentException("Unsupported grant_type: " + grantType);
        }

        return ResponseEntity.ok(response);
    }

    // 예외 처리 핸들러 (선택 사항이지만 권장)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + e.getMessage());
    }
}