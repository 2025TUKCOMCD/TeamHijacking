package com.example.back.controller;

import com.example.back.dto.UserDTO;
import com.example.back.domain.User;
import com.example.back.repository.UserRepository;
import com.example.back.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
        try {
            log.info("회원 등록 요청 - loginId: {}, name: {}", userDTO.getLoginId(), userDTO.getName());

            // 필수 값 검사
            if (userDTO.getLoginId() == null || userDTO.getLoginId().isEmpty() ||
                    userDTO.getName() == null || userDTO.getName().isEmpty()) {
                log.warn("입력값 부족 - loginId: {}, name: {}", userDTO.getLoginId(), userDTO.getName());
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("message", "이름과 로그인 ID는 필수입니다."));
            }

            if(userRepository.existsByLoginId(userDTO.getLoginId())) {
                log.warn("이미 등록된 사용자 - loginId: {}", userDTO.getLoginId());
                //JSON 형태로 변경
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Collections.singletonMap("message", "이미 등록된 사용자입니다."));
            }

            //사용자 저장
            User savedUser = userService.saveUser(userDTO);
            log.info("사용자 등록 성공 - id: {}", savedUser.getId());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedUser);

//        } catch (IllegalStateException e) {
//            log.warn("중복 사용자 등록 시도 - loginId: {}", userDTO.getLoginId());
//            return ResponseEntity
//                    .status(HttpStatus.CONFLICT)
//                    .body("이미 등록된 사용자입니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("message", e.getMessage()));

        } catch (Exception e) {
            log.error("사용자 등록 중 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}