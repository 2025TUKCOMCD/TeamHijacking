package com.example.back.dto.kakao;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoUserInfoResponse {
    private Long id; // 카카오 사용자 고유 ID
    private Boolean hasSignedUp; // 사용자가 회원가입을 했는지 여부 (카카오싱크 사용 시)
    private LocalDateTime connectedAt; // 서비스와 연결된 시각 (ISO 8601)
    private Map<String, Object> properties; // 카카오계정 프로필 정보 (닉네임, 프로필 이미지 등)
    private KakaoAccount kakaoAccount; // 카카오계정 정보 (이메일, 성별 등)

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KakaoAccount {
        private Boolean profileNeedsAgreement;
        private Map<String, Object> profile; // 닉네임, 프로필 이미지 URL 등
        private Boolean emailNeedsAgreement;
        private String email;
        // 추가적으로 필요한 정보 (성별, 연령대 등)는 여기에 추가하고 카카오 동의 항목 설정 필요
    }
}