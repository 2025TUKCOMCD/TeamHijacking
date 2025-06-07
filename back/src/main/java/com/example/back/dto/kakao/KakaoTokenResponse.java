package com.example.back.dto.kakao;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 카카오 API 응답 필드명과 자바 필드명 매핑을 위한 어노테이션
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoTokenResponse {
    private String accessToken;
    private String tokenType;
    private String refreshToken;
    private Integer expiresIn; // 초 단위
    private Integer refreshTokenExpiresIn; // 초 단위
    private String scope;
}