package com.example.back.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

@Data
public class UserDTO {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer id;           // `int unsigned`에 대응되는 Integer

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;          // `varchar(255)`에 대응되는 String

    private Timestamp create_at;   // `datetime`
    private Timestamp update_at;   // `datetime`에 대응되는 Timestamp

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String loginId; // `varchar(255)`에 대응되는 String

//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    private String password; // `varchar(255)`에 대응되는 String
}