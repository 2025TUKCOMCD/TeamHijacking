package com.example.back.repository;

import com.example.back.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
   //1. 액세스 할 엔티티 클래스 정의:: 완료. User 클래스 가 그것임.

   //Login_id를 이용해 유저 찾기(Native Query)
   @Query(value = "SELECT * FROM `user` u WHERE u.login_id = :loginId", nativeQuery = true)    //Login_id를 통해 멤버를 찾는 식.
   User findByLoginId(@Param("loginId") String loginId);

   boolean existsByLoginId(String loginId);
}

