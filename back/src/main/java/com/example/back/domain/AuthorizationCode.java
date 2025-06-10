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
@Table(name = "authorization_codes") // 데이터베이스 테이블 이름과 일치해야 합니다.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // CreatedDate, LastModifiedDate 사용을 위해 필요
public class AuthorizationCode {
    @Id
    private String code; // PRIMARY KEY

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "redirect_uri", nullable = false)
    private String redirectUri;

    @Column(name = "user_id", nullable = false)
    private String userId;

    private String scopes; // 콤마로 구분된 권한 범위

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // TIMESTAMP 타입은 LocalDateTime으로 매핑됩니다.

    @Column(name = "used", nullable = false)
    private boolean used; // 코드 사용 여부

    @CreatedDate // 엔티티가 처음 저장될 때 현재 시간 자동 설정
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate // 엔티티가 업데이트될 때 현재 시간 자동 설정
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}