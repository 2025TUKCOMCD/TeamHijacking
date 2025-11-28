package com.example.back.repository;

import com.example.back.domain.AuthorizationCode; // 필요하다면 유지
import com.example.back.domain.SavedRoute;      // SavedRoute 엔티티 임포트 (필수)
import com.example.back.domain.User;            // User 엔티티 임포트 (findByUser 사용 시 필요)
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // 일반적으로 리포지토리 인터페이스에는 직접 붙이지 않음

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SavedRouteRepository extends JpaRepository<SavedRoute, Integer> {

     List<SavedRoute> findByUser_LoginId(String loginId);

    Optional<SavedRoute> findBySavedRouteName(String savedRouteName);

    // SavedRoute 엔티티 내의 'user' 필드를 통해 User 엔티티의 'loginId' 필드에 접근
    Optional<SavedRoute> findByDepartureNameAndDestinationNameAndStartLatAndStartLngAndEndLatAndEndLngAndUser_LoginId(
            String departureName, String destinationName, BigDecimal startLat, BigDecimal startLng,
            BigDecimal endLat, BigDecimal endLng, String loginId // 이 loginId는 User의 loginId와 매핑됩니다.
    );
    // SavedRoute 엔티티 내의 'user' 필드를 통해 User 엔티티의 'loginId' 필드 접근 및 transroute_key가 일치 시 업데이트
    Optional<SavedRoute> findByTransportrouteKeyAndUser(Integer transportrouteKey, User user);

}