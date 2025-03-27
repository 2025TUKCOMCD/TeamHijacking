package com.example.back.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.*;

import java.time.LocalDateTime;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  //AUTO_INCREMENT
    private Integer id;                                  //기본 키 (Integer 타입)

    @Column(nullable = false)
    private String name;

    @Column(name = "login_id", unique = true, nullable = false)
    private String loginId;

//    @Column(nullable = false)
//    private String password;

    @Column(name = "create_at", nullable = false, updatable = false)
    private Timestamp createAt;

    @Column(name = "update_at", nullable = false)
    private Timestamp updateAt;
}
