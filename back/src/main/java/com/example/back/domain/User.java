package com.example.back.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "`user`")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  //AUTO_INCREMENT
    private Integer id;                                  //기본 키 (Integer 타입)

    @Column(nullable = false)
    private String name;

    @Column(name = "login_id", unique = true, nullable = false)
    private String loginId;    //여기에 카카오 로그인 을 받아 오기로 함

    @Column(name = "create_at", nullable = false, updatable = false)
    private Timestamp createAt;

    @Column(name = "update_at", nullable = false)  //@Column이 자동 변환을 시켜중
    private Timestamp updateAt;

    @Column(name = "email", length = 320)
    private String email;

    /*@NoArgConstructor 와 @AllArgsConstructor 를 이용해 생성자 자동 생성 */
    @PrePersist
    protected void onCreate() {
        this.createAt = Timestamp.valueOf(LocalDateTime.now());
        this.updateAt = Timestamp.valueOf(LocalDateTime.now());
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateAt = Timestamp.valueOf(LocalDateTime.now());
    }
}
