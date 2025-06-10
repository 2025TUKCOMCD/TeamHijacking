package com.example.back.repository;

import com.example.back.domain.SmartThingsToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmartThingsTokenRepository extends JpaRepository<SmartThingsToken, Long> {
    // 사용자 ID로 토큰 정보를 조회하는 메서드 추가
    Optional<SmartThingsToken> findByUserId(String userId);
}