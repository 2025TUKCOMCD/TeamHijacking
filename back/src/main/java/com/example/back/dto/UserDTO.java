package com.example.back.dto; // DTO의 정확한 패키지 경로를 사용해 주세요.

import com.example.back.domain.User; // User 엔티티 클래스를 임포트합니다.
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true) // JSON 역직렬화 시 알 수 없는 속성 무시
public class UserDTO {
    private Integer id;    // User 엔티티의 기본 키(PK)인 'id'와 대응
    private String name;   // User 엔티티의 'name' 필드와 대응
    private String email;  // User 엔티티의 'email' 필드와 대응
    private String loginId; // User 엔티티의 'loginId' 필드와 대응


    public User toEntity(){
        return User.builder()
                // ID는 DB에서 자동 생성되므로, 새로운 User 생성 시에는 이 필드를 포함하지 않습니다.
                // 만약 기존 User를 업데이트하는 경우라면, id(this.id)를 추가할 수 있습니다.
                .name(this.name)
                .loginId(this.loginId)
                .email(this.email)
                .build();
    }


    public static UserDTO fromEntity(User user) {
        if (user == null) {
            return null; // 또는 IllegalArgumentException 발생, 상황에 따라 처리
        }
        return UserDTO.builder()
                .id(user.getId())       // 엔티티의 ID를 DTO에 매핑
                .name(user.getName())
                .loginId(user.getLoginId())
                .email(user.getEmail())
                .build();
    }
}