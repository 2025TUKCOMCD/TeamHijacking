package com.example.back.service;

import com.example.back.dto.UserDTO;
import com.example.back.domain.User;
import com.example.back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/*Lombok 으로 스프링 에서 DI 방법 중 생성자 주입을 임의의 코드 없이 자동 으로 설정. 초기화 되지 않은
final 필드나 @NonNull 이 붙은 필드에 대해 생성자 생성. 새로운 필드 추가 시 다시 생성자 안 만들어도 됨
chatGPT 의 도움 받아 @Autowired 대신 생성자 다른 걸로 주입*/
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User saveUser(UserDTO userDTO) {
        if (userDTO.getName() == null || userDTO.getLoginId() == null || userDTO.getEmail() == null) {
            throw new IllegalArgumentException("이름과 로그인 ID, 이메일은 필수입니다.");
        }

        // loginId 중복 확인
        if (userRepository.existsByLoginId(userDTO.getLoginId())) {
            throw new IllegalStateException("이미 등록된 사용자입니다.");
        }

        User user = userDTO.toEntity();
        return userRepository.save(user);
        //id는 자동 생성되므로, saveUser()에서는 id를 직접 설정하지 않음
    }
}
