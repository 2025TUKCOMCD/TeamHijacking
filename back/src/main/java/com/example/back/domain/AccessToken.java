package com.example.back.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "access_tokens") // 데이터베이스 테이블 이름과 일치해야 합니다.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // CreatedDate, LastModifiedDate 사용을 위해 필요
public class AccessToken {
    @Id
    private String token; // PRIMARY KEY (액세스 토큰 또는 리프레시 토큰 자체)

    @Column(name = "token_type", nullable = false)
    private String tokenType; // "access_token" 또는 "refresh_token"

    @Column(name = "user_id", nullable = false)
    private String userId; // 카카오 사용자 고유 ID

    @Column(name = "client_id", nullable = false)
    private String clientId; // SmartThings 앱의 클라이언트 ID

    private String scopes; // 부여된 권한 범위

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // 토큰 만료 시간

    @CreatedDate // 엔티티가 처음 저장될 때 현재 시간 자동 설정
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate // 엔티티가 업데이트될 때 현재 시간 자동 설정
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "refresh_token_id")
    private String refreshTokenId; // 액세스 토큰이 리프레시 토큰에 의해 발급된 경우, 해당 리프레시 토큰의 ID (NULL 허용)
}