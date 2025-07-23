package com.example.back.controller;

import com.example.back.dto.UserDTO;
import com.example.back.domain.User;
import com.example.back.repository.UserRepository;
import com.example.back.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

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
            log.info("사용자 등록 요청 수신: {}", userDTO);  // ✅ 서버 로그 확인
            System.out.println(userDTO);
            log.info("회원 등록 요청 - loginId: {}, name: {}", userDTO.getLoginId(), userDTO.getName());

            // 필수 값 검사
            if (userDTO.getLoginId() == null || userDTO.getLoginId().isEmpty() ||
                    userDTO.getName() == null || userDTO.getName().isEmpty()) {
                log.warn("입력값 부족 - loginId: {}, name: {}", userDTO.getLoginId(), userDTO.getName());
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("message", "이름과 로그인 ID는 필수 입니다."));
            }

            if(userRepository.existsByLoginId(userDTO.getLoginId())) {
                log.warn("이미 등록된 사용자 - loginId: {}", userDTO.getLoginId());
                //JSON 형태로 변경
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Collections.singletonMap("message", "이미 등록된 사용자 입니다."));
            } else {
                //사용자 저장
                User savedUser = userService.saveUser(userDTO);
                log.info("사용자 등록 성공 - id: {}", savedUser.getId());
                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(savedUser);
            }

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

    // 🔹 사용자 조회 (loginId 기반)
    @GetMapping("/{loginId}")
    public ResponseEntity<?> getUserByLoginId(@PathVariable String loginId) {
        try {
            log.info("사용자 조회 요청 - loginId: {}", loginId);

            // userRepository.findByLoginId()는 Optional<User>를 반환합니다.
            Optional<User> userOptional = userRepository.findByLoginId(loginId);

            // Optional을 처리하여 User가 존재하는지 확인합니다.
            if (userOptional.isPresent()) {
                // User 엔티티를 가져와 DTO로 변환합니다.
                User user = userOptional.get();
                UserDTO userDTO = UserDTO.fromEntity(user); // fromEntity 메서드 사용

                return ResponseEntity.ok(userDTO);
            } else {
                // 사용자를 찾을 수 없는 경우 NOT_FOUND (404) 응답을 반환합니다.
                log.warn("사용자를 찾을 수 없습니다. loginId: {}", loginId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("message", "사용자를 찾을 수 없습니다."));
            }
        } catch (Exception e) {
            // 예외 발생 시 INTERNAL_SERVER_ERROR (500) 응답을 반환합니다.
            log.error("사용자 조회 중 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "서버 오류가 발생했습니다.")); // 사용자에게 더 친절한 메시지
        }
    }

    @PutMapping("/{loginId}/nickname")
    public ResponseEntity<?> updateNickname (
            @PathVariable String loginId,
            @RequestBody Map<String, String> updateData
    ) {
        try {
            String newName = updateData.get("name");
            if (newName == null || newName.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("message", "닉네임은 비워둘 수 없습니다."));
            }

            User updatedUser = userService.updateNickname(loginId, newName);
            return ResponseEntity.ok(UserDTO.fromEntity(updatedUser));

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", e.getMessage()));
        } catch (Exception e) {
            log.error("닉네임 변경 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "서버 오류가 발생했습니다."));
        }
    }
}