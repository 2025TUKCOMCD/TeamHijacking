// SmartThingsAuthController.java (이 코드로 당신의 파일을 완전히 교체하세요)
package com.example.back.controller; // TODO: 당신의 실제 패키지 경로로 변경

import com.example.back.service.SmartThingsAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Lombok Slf4j 어노테이션이 다시 필요하다면 여기 활성화
import org.springframework.stereotype.Controller; // @RestController 대신 @Controller 사용
import org.springframework.ui.Model; // HTML 템플릿에 데이터 전달용
import org.springframework.web.bind.annotation.GetMapping; // GET 요청으로 변경
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController // View를 반환하므로 @Controller 사용
@RequestMapping("/callback") // SmartThings redirect_uri와 일치해야 합니다. (예: https://seemore.io.kr/callback)
@RequiredArgsConstructor
@Slf4j // 로깅 사용을 원한다면 주석 해제 (IntelliJ Lombok 설정 필요)
public class SmartThingsAuthController {

    private final SmartThingsAuthService smartThingsAuthService;


    // @RequiredArgsConstructor가 생성자를 자동으로 만들어주므로 수동으로 추가할 필요 없습니다.
    // public SmartThingsAuthController(SmartThingsAuthService smartThingsAuthService) {
    //     this.smartThingsAuthService = smartThingsAuthService;
    // }


    /**
     * SmartThings로부터 authorization code를 GET 요청으로 받아 처리하는 엔드포인트
     * redirect_uri (예: https://seemore.io.kr/callback)
     *
     * @param code SmartThings에서 전달하는 인증 코드
     * @param state SmartThings에서 전달하는 상태 값 (여기에 사용자 ID 포함 권장)
     * @param model Thymeleaf 템플릿에 데이터를 전달하기 위한 Model 객체
     * @return HTML 템플릿 이름 (성공 또는 실패 페이지)
     */

    @GetMapping // 이 @GetMapping은 클래스 레벨의 @RequestMapping("/callback")에 대한 상대 경로를 의미합니다.
    public String handleSmartThingsCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state, // state는 필수가 아닐 수 있음
            @RequestParam(value = "error", required = false) String error, // SmartThings에서 에러 발생 시
            @RequestParam(value = "error_description", required = false) String errorDescription,
            Model model
    ) {
        // System.out.println 대신 log.info를 사용하려면 @Slf4j 주석 해제 및 Lombok 설정 필요

        // 1. SmartThings 자체에서 에러가 발생한 경우
        if (error != null) {
            model.addAttribute("success", false);
            model.addAttribute("message", "SmartThings 인증에 실패했습니다: " + (errorDescription != null ? errorDescription : error));
            model.addAttribute("androidDeepLink", buildAndroidDeepLink(false, model.getAttribute("message").toString(), null));
            return "auth-result";
        }

        String userId = null;
        String csrfToken = null;
        if (state != null && state.contains("_")) {
            String[] parts = state.split("_", 2);
            userId = parts[0];
            csrfToken = parts[1];
        }

        if (code == null || userId == null || userId.trim().isEmpty()) {
            model.addAttribute("success", false);
            model.addAttribute("message", "SmartThings 연동에 필요한 정보가 부족합니다.");
            model.addAttribute("androidDeepLink", buildAndroidDeepLink(false, model.getAttribute("message").toString(), null));
            return "auth-result";
        }

        try {
            System.out.println(code+ " " + state + " " + userId); // 디버깅용 출력
            smartThingsAuthService.exchangeCodeForTokens(code, state, userId);

            model.addAttribute("success", true);
            model.addAttribute("message", "SmartThings 연동이 성공적으로 완료되었습니다!");
            String backendAuthToken = "your_backend_session_token_if_any";
            model.addAttribute("androidDeepLink", buildAndroidDeepLink(true, model.getAttribute("message").toString(), backendAuthToken));

            return "auth-result";
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("message", "SmartThings 연동 처리 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("androidDeepLink", buildAndroidDeepLink(false, model.getAttribute("message").toString(), null));
            return "auth-result";
        }
    }

    private String buildAndroidDeepLink(boolean success, String message, String backendAuthToken) {
        String androidScheme = "mysmartthingsapp";
        String androidHost = "auth";

        StringBuilder deepLinkBuilder = new StringBuilder();
        deepLinkBuilder.append(androidScheme).append("://").append(androidHost);
        deepLinkBuilder.append("?status=").append(success ? "success" : "failure");
        deepLinkBuilder.append("&message=").append(URLEncoder.encode(message, StandardCharsets.UTF_8));

        if (backendAuthToken != null && !backendAuthToken.isEmpty()) {
            deepLinkBuilder.append("&token=").append(URLEncoder.encode(backendAuthToken, StandardCharsets.UTF_8));
        }
        return deepLinkBuilder.toString();
    }
}