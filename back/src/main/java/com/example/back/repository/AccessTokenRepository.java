package com.example.back.repository;

import com.example.back.domain.AccessToken; // 엔티티 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, String> {
    // 액세스 토큰 값으로 엔티티를 찾는 메서드
    Optional<AccessToken> findByToken(String token);

    // 특정 사용자의 액세스 토큰을 찾는 메서드 (필요에 따라 추가)
    List<AccessToken> findByUserIdAndTokenType(String userId, String tokenType);

    // 리프레시 토큰 ID로 액세스 토큰을 찾는 메서드 (리프레시 토큰 갱신 시 필요할 수 있음)
    Optional<AccessToken> findByRefreshTokenId(String refreshTokenId);

    // 특정 사용자, 클라이언트 ID, 토큰 타입으로 토큰을 찾는 메서드 (필요에 따라 추가)
    Optional<AccessToken> findByUserIdAndClientIdAndTokenType(String userId, String clientId, String tokenType);
}