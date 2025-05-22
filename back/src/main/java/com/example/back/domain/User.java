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

    /*entity 는 setter 를 사용 하지 않는다 고 한다. 대신 생성자 를 사용 한다고.
    @NoArgConstructor 와 @AllArgsConstructor 를 이용해 생성자 는 자동 생성 된다.  */

    /*Entity 가 Persist(데이터 베이스 에 삽입) 또는 Update(데이터 베이스 에 수정) 되기 전
    JPA Provider 가 자동 으로 실행 해야 하는 메서드 를 지정 하는 데 사용 되는 JPA annotation.
    PrePersist annotation 은 Entity 가 영속화 되기 직전에 실행 되어야 하는 Entity class 의 method 를
    표시 하는 데에 사용 하는 것. Entity 가 데이터 베이스 에 저장 되기 전 JPA 의 annotation 에 의해 사용 된다.
    PreUpdate 는 Entity 가 update 되기 직전에 실행 되어야 하는 entity 클래스 의 메서드 를 표시 하는 데에
    사용 된다. */
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
