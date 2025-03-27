package com.example.back.repository;

import com.example.back.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

   // @Query(value = "SELECT ")
   User findById(Integer Id);
}

