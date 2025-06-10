package com.example.back.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "smartthings_tokens") // DB 테이블 이름
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SmartThingsToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary Key

    @Column(nullable = false)
    private String accessToken; // SmartThings Access Token

    @Column(nullable = false)
    private String refreshToken; // SmartThings Refresh Token

    @Column(nullable = false)
    private Long expiresIn; // Access Token 유효 기간 (초 단위)

    @Column(nullable = false)
    private String scope; // 부여된 스코프

    @Column(nullable = false)
    private LocalDateTime createdAt; // 토큰 생성 시간

    private LocalDateTime expiresAt; // Access Token 만료 시간 계산용

    // 토큰 소유자를 식별하는 필드 (예: 사용자 ID).
    // 이 userId는 안드로이드 앱에서 당신의 백엔드로 넘겨주는 userId와 동일해야 합니다.
    @Column(nullable = false, unique = true) // 사용자당 하나의 SmartThings 연동을 가정하여 unique 설정
    private String userId; // TODO: 실제 사용자 관리 시스템이 있다면 그 사용자 ID와 연결

    // 토큰 만료 시간 설정
    @PrePersist // 엔티티가 저장되기 전에 호출
    @PreUpdate // 엔티티가 업데이트되기 전에 호출 (토큰 갱신 시 필요)
    public void calculateExpiresAt() {
        if (expiresIn != null) {
            this.expiresAt = LocalDateTime.now().plusSeconds(expiresIn);
        }
    }
}