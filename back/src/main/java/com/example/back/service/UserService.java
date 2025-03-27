package com.example.back.service;

import com.example.back.dto.UserDTO;
import com.example.back.entity.User;
import com.example.back.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User saveUser(UserDTO userDTO) {
        User user = new User();
        user.setName(userDTO.getName());
        user.setLoginId(userDTO.getLoginId());
        //user.setPassword(userDTO.getPassword());
        user.setCreateAt(Timestamp.valueOf(LocalDateTime.now()));
        user.setUpdateAt(Timestamp.valueOf(LocalDateTime.now()));

        return userRepository.save(user);
        //id는 자동 생성되므로, saveUser()에서는 id를 직접 설정하지 않음
    }
}
