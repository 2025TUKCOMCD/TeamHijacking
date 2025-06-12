package com.example.back.dto;

import com.example.back.domain.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {
    private Integer id;           // `int unsigned`에 대응되는 Integer
    private String name;          // `varchar(255)`에 대응되는 String
    private String email;          //varchar(360)에 대응되어야 하는 String
    private String loginId; // `varchar(255)`에 대응되는 String

    //DTO -> Entity 변환
    public User toEntity(){
        return User.builder()
                .name(this.name)
                .loginId(this.loginId)
                .email(this.email)
                .build();
    }
}