package com.example.back.dto;

import com.example.back.domain.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Integer id;           // `int unsigned`에 대응되는 Integer
    private String name;          // `varchar(255)`에 대응되는 String
    private String email;          //varchar(360)에 대응되어야 하는 String
    private String loginId; // `varchar(255)`에 대응되는 String
//    private Timestamp createAt;   // `datetime`
//    private Timestamp updateAt;   // `datetime`에 대응되는 Timestamp

    // 생성/수정 시간 은 DTO 에 포함 하지 말거나 클라이언트 요청에 포함하지 말자.
    // @JsonIgnore
    // private Timestamp createAt;
    // @JsonIgnore
    // private Timestamp updateAt;

    //DTO -> Entity 변환
    public User toEntity(){
        return User.builder()
                .name(this.name)
                .loginId(this.loginId)
                .email(this.email)
//                .createAt(Timestamp.valueOf(LocalDateTime.now()))
//                .updateAt(Timestamp.valueOf(LocalDateTime.now()))
                // createAt, updateAt 은 엔티티의 @PrePersist가 처리
                .build();
    }

    /* 더 나은 방법으로는
    public User toEntity() {
    return User.builder()
            .name(this.name)
            .loginId(this.loginId)
            .build();
    }
    가 있다고 하니, 추후 수정하는 것도 나쁘지 않을 성 싶음 */
}