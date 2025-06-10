package com.example.back.service;

import com.example.back.domain.SmartThingsToken;
import com.example.back.repository.SmartThingsTokenRepository;
import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j; // 이 어노테이션은 이제 필요 없습니다.
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SmartThingsAuthService {

    // private static final Logger log = LoggerFactory.getLogger(SmartThingsAuthService.class); // 필요하다면 이렇게 직접 선언할 수도 있지만, System.out으로 대체했으니 제거

    private final SmartThingsTokenRepository smartThingsTokenRepository;
    private final WebClient webClient;

    @Value("${smartthings.client-id}")
    private String clientId;

    @Value("${smartthings.client-secret}")
    private String clientSecret;

    @Value("${smartthings.redirect-uri}")
    private String redirectUri;

    @Value("${smartthings.token-url}")
    private String tokenUrl;


    public SmartThingsToken exchangeCodeForTokens(String code, String state, String userId) {
        System.out.println("Attempting to exchange code for tokens for userId: " + userId); // log.info 대신 System.out.println 사용

        try {
            Map<String, Object> tokenResponse = webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                            .with("client_id", clientId)
                            .with("client_secret", clientSecret)
                            .with("code", code)
                            .with("redirect_uri", redirectUri))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), response -> response.bodyToMono(String.class).map(body -> new WebClientResponseException(response.statusCode().value(), response.statusCode().toString(), null, body.getBytes(), null)))
                    .onStatus(status -> status.is5xxServerError(), response -> response.bodyToMono(String.class).map(body -> new WebClientResponseException(response.statusCode().value(), response.statusCode().toString(), null, body.getBytes(), null)))
                    .bodyToMono(Map.class)
                    .block();
            System.out.println("SmartThings token exchange response: " + tokenResponse); // log.info 대신 System.out.println 사용

            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                System.err.println("SmartThings token exchange failed. No access_token in response: " + tokenResponse); // 오류는 System.err.println
                throw new RuntimeException("SmartThings token exchange failed: Invalid response.");
            }

            String accessToken = (String) tokenResponse.get("access_token");
            String refreshToken = (String) tokenResponse.get("refresh_token");
            Long expiresIn = Long.valueOf(tokenResponse.get("expires_in").toString());
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

            SmartThingsToken savedToken = smartThingsTokenRepository.save(smartThingsToken);
            System.out.println("SmartThings tokens saved/updated successfully for userId: " + userId); // log.info 대신 System.out.println
            return savedToken;

        } catch (WebClientResponseException e) {
            System.err.println("Error during SmartThings token exchange (WebClient error). Status: " + e.getStatusCode() + ", Body: " + e.getResponseBodyAsString());
            e.printStackTrace(); // 스택 트레이스 출력
            throw new RuntimeException("SmartThings API error during token exchange: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Unexpected error during SmartThings token exchange for userId " + userId + ": " + e.getMessage());
            e.printStackTrace(); // 스택 트레이스 출력
            throw new RuntimeException("Internal server error during token exchange: " + e.getMessage());
        }
    }

    public SmartThingsToken refreshAccessToken(String refreshToken, String userId) {
        System.out.println("Attempting to refresh access token for userId: " + userId); // log.info 대신 System.out.println
        try {
            Map<String, Object> tokenResponse = webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                            .with("client_id", clientId)
                            .with("client_secret", clientSecret)
                            .with("refresh_token", refreshToken))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), response -> response.bodyToMono(String.class).map(body -> new WebClientResponseException(response.statusCode().value(), response.statusCode().toString(), null, body.getBytes(), null)))
                    .onStatus(status -> status.is5xxServerError(), response -> response.bodyToMono(String.class).map(body -> new WebClientResponseException(response.statusCode().value(), response.statusCode().toString(), null, body.getBytes(), null)))
                    .bodyToMono(Map.class)
                    .block();

            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                System.err.println("SmartThings token refresh failed. No access_token in response: " + tokenResponse);
                throw new RuntimeException("SmartThings token refresh failed: Invalid response.");
            }

            String newAccessToken = (String) tokenResponse.get("access_token");
            String newRefreshToken = (String) tokenResponse.getOrDefault("refresh_token", refreshToken);
            Long newExpiresIn = Long.valueOf(tokenResponse.get("expires_in").toString());
            String newScope = (String) tokenResponse.get("scope");

            SmartThingsToken smartThingsToken = smartThingsTokenRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("No existing token found for userId: " + userId));

            smartThingsToken.setAccessToken(newAccessToken);
            smartThingsToken.setRefreshToken(newRefreshToken);
            smartThingsToken.setExpiresIn(newExpiresIn);
            smartThingsToken.setScope(newScope);
            smartThingsToken.setCreatedAt(LocalDateTime.now());
            smartThingsToken.calculateExpiresAt();

            SmartThingsToken savedToken = smartThingsTokenRepository.save(smartThingsToken);
            System.out.println("SmartThings token refreshed successfully for userId: " + userId);
            return savedToken;

        } catch (WebClientResponseException e) {
            System.err.println("Error during SmartThings token refresh (WebClient error). Status: " + e.getStatusCode() + ", Body: " + e.getResponseBodyAsString());
            e.printStackTrace();
            throw new RuntimeException("SmartThings API error during token refresh: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Unexpected error during SmartThings token refresh for userId " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Internal server error during token refresh: " + e.getMessage());
        }
    }
}