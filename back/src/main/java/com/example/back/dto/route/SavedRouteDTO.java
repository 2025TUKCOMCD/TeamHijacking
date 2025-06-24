package com.example.back.dto.route; // 올바른 패키지 경로 확인

import com.example.back.domain.SavedRoute;
import com.example.back.domain.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedRouteDTO {
    private Integer transportrouteKey; // DB에서 자동 생성되는 키, DTO에서는 입력받지 않음
    private String departureName;
    private String destinationName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime whenFirstGo; // DB insertable = false 이므로, DTO에서 입력받지 않거나, 조회 시 사용
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime whenLastGo;  // DB insertable = false 이므로, DTO에서 입력받지 않거나, 조회 시 사용
    private Integer userRouteCount; // 엔티티와 필드명 일치
    private Boolean isFavorite;
    private BigDecimal startLat;
    private BigDecimal startLng;
    private BigDecimal endLat;
    private BigDecimal endLng;
    private String savedRouteName;

    // 필드명 변경: userId -> loginId (User 엔티티의 loginId와 매핑)
    private String loginId; // 클라이언트로부터 받을 사용자의 로그인 ID (예: 카카오 ID)

    // DTO를 엔티티로 변환하는 메서드 (User 객체를 인자로 받음)
    public SavedRoute toEntity(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null when converting DTO to SavedRoute Entity.");
        }

        return SavedRoute.builder()
                .departureName(this.departureName)
                .destinationName(this.destinationName)
                .userRouteCount(this.userRouteCount)
                .isFavorite(this.isFavorite)
                .startLat(this.startLat)
                .startLng(this.startLng)
                .endLat(this.endLat)
                .endLng(this.endLng)
                .savedRouteName(this.savedRouteName)
                .user(user) // User 엔티티 객체를 직접 설정하여 SavedRoute와 연결
                // whenFirstGo, whenLastGo는 DB에서 처리하거나 다른 로직에서 주입하므로 여기서는 제외
                .build();
    }

    // 엔티티를 DTO로 변환하는 정적 팩토리 메서드
    public static SavedRouteDTO fromEntity(SavedRoute entity) {
        return SavedRouteDTO.builder()
                .transportrouteKey(entity.getTransportrouteKey())
                .departureName(entity.getDepartureName())
                .destinationName(entity.getDestinationName())
                .whenFirstGo(entity.getWhenFirstGo())
                .whenLastGo(entity.getWhenLastGo())
                .userRouteCount(entity.getUserRouteCount())
                .isFavorite(entity.getIsFavorite())
                .startLat(entity.getStartLat())
                .startLng(entity.getStartLng()) // entity.getLng()는 오타일 가능성이 있으므로, getEndLng() 또는 getStartLng() 확인
                .endLat(entity.getEndLat())
                .endLng(entity.getEndLng())
                .savedRouteName(entity.getSavedRouteName())
                .loginId(entity.getUser() != null ? entity.getUser().getLoginId() : null) // User 객체에서 loginId를 가져옴
                .build();
    }
}