package com.example.back.dto.kakao;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// SmartThings OAuth 2.0 스펙에 맞춰 필드명 매핑
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenResponse {
    private String accessToken;
    private String tokenType = "Bearer"; // OAuth 2.0 표준에 따라 Bearer 고정
    private Integer expiresIn; // 초 단위
    private String refreshToken;
    private String scope; // 부여된 권한 범위 (콤마로 구분)
}