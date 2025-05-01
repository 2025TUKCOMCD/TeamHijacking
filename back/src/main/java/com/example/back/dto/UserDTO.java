package com.example.back.dto;

import com.example.back.domain.User;
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
    private String loginId; // `varchar(255)`에 대응되는 String
    private Timestamp createAt;   // `datetime`
    private Timestamp updateAt;   // `datetime`에 대응되는 Timestamp

    //DTO -> Entity 변환
    public User toEntity(){
        return User.builder()
                .name(this.name)
                .loginId(this.loginId)
                .createAt(Timestamp.valueOf(LocalDateTime.now()))
                .updateAt(Timestamp.valueOf(LocalDateTime.now()))
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