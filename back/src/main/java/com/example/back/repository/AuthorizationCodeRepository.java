package com.example.back.repository;

import com.example.back.domain.AuthorizationCode; // 엔티티 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorizationCodeRepository extends JpaRepository<AuthorizationCode, String> {
    // 인가 코드로 엔티티를 찾는 메서드 (PRIMARY KEY는 JpaRepository에 포함)
    Optional<AuthorizationCode> findByCode(String code);

    // 추가적으로 필요한 쿼리 메서드가 있다면 여기에 정의합니다.
    // 예: 만료된 코드 찾기 등
}