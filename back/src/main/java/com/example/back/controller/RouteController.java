package com.example.back.controller;

import com.example.back.domain.SavedRoute;
import com.example.back.domain.User;
import com.example.back.dto.RealTimeResultDTO;
import com.example.back.dto.ResultDTO;
import com.example.back.dto.SavedRouteResponseDTO;
import com.example.back.dto.realtime.RealtimeDTO;
import com.example.back.dto.route.RouteDTO;
import com.example.back.dto.route.SavedRouteDTO;
import com.example.back.service.DatabaseService;
import com.example.back.service.RealLocationService;
import com.example.back.service.RouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Slf4j // 로깅을 위한 Lombok 어노테이션
@RequestMapping("/api")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @Autowired
    private RealLocationService realLocationService;

    @Autowired
    private DatabaseService databaseService;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/route")
    public ResponseEntity<List<ResultDTO>> getRoute(@RequestBody RouteDTO routeDTO) {
        List<ResultDTO> response = routeService.fetchAndProcessRoutes(routeDTO);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/realTime")
    public ResponseEntity<RealTimeResultDTO> getRealBusRoute(@RequestBody RealtimeDTO realtimeDTO)  {
        System.out.println("Received RealtimeDTO: " + realtimeDTO);
        RealTimeResultDTO response = realLocationService.getRealTime(realtimeDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/DBSaveRoute")
    public ResponseEntity<SavedRouteResponseDTO> saveRoute(@RequestBody SavedRouteDTO savedRouteDTO) {
        log.info("Received SavedRouteDTO: {}", savedRouteDTO);

        String loginId = savedRouteDTO.getLoginId();

        // 1. loginId로 User를 조회
        Optional<User> userOptional = databaseService.findUserByLoginId(loginId);

        // 2. User가 존재하는지 확인하고 처리
        if (userOptional.isEmpty()) {
            log.warn("User not found with loginId: {}", loginId);
            // 사용자를 찾을 수 없는 경우 404 NOT FOUND 응답과 함께 SavedRouteResponseDTO 반환
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SavedRouteResponseDTO.builder()
                            .success(false)
                            .message("User with login ID '" + loginId + "' not found.")
                            .build());
        }

        // 3. Optional에서 User 객체를 안전하게 가져옴
        User user = userOptional.get();
        Integer userId = user.getId();

        try {
            SavedRouteResponseDTO responseDTO = null;
            if(savedRouteDTO.getTransportrouteKey()!=0){
                responseDTO = databaseService.updateRoute(savedRouteDTO, userId);
            }else{
                responseDTO = databaseService.saveRoute(savedRouteDTO, userId);
            }
            // 4. 경로 저장 서비스 호출. 이제 이 메서드는 SavedRouteResponseDTO를 반환해야 합니다.


            // 서비스에서 이미 성공/실패 여부를 DTO에 담아주므로, 별도의 로그와 OK/ERROR 분기 처리
            if (responseDTO.isSuccess()) {
                log.info("Route save/update successful for user ID: {}. Details: {}", userId, responseDTO);
                return ResponseEntity.ok(responseDTO);
            } else {
                // 서비스 내부에서 발생한 특정 실패 케이스 (예: 데이터 유효성 검사 실패 등)
                log.warn("Route save/update failed for user ID {}: {}", userId, responseDTO.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDTO);
            }

        } catch (Exception e) {
            log.error("Unhandled error saving route for user ID {}: {}", userId, e.getMessage(), e);
            // 예상치 못한 예외 발생 시 500 INTERNAL SERVER ERROR 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SavedRouteResponseDTO.builder()
                            .success(false)
                            .message("An unexpected error occurred while saving the route: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/DBGetRoute") // API 엔드포인트
    public ResponseEntity<List<SavedRouteDTO>> getRoutes(@RequestParam String loginId) { // 단일 DTO가 아닌 List<SavedRouteDTO> 반환
        log.info("Received request to get routes for loginId: {}", loginId);

        // 1. loginId로 User를 조회
        Optional<User> userOptional = databaseService.findUserByLoginId(loginId);

        // 2. User가 존재하는지 확인하고 처리
        if (userOptional.isEmpty()) {
            log.warn("User not found with loginId: {}", loginId);
            // 사용자를 찾을 수 없는 경우 404 NOT FOUND 응답 반환
            // 클라이언트는 이 상태 코드를 보고 사용자가 없음을 알 수 있습니다.
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null); // 바디에 아무것도 넣지 않거나, 특정 에러 DTO를 반환할 수 있습니다.
        }

        // 3. Optional에서 User 객체를 안전하게 가져옴
        User user = userOptional.get();
        // Integer userId = user.getId(); // userId는 이제 databaseService.getSavedRoutesByUserId 호출에만 사용될 수 있습니다.

        // 4. 경로 조회 서비스 호출
        // databaseService.getSavedRoutesByUserId(userId) 대신,
        // loginId로 직접 SavedRoute를 조회하는 것이 더 효율적일 수 있습니다.
        // 또는, databaseService.getSavedRoutesByLoginId(loginId)를 사용하는 것을 고려하세요.
        // 여기서는 기존 메서드를 재사용하되, UserRepository에서 User를 찾는 로직을 추가하여 연결합니다.

        List<SavedRoute> savedRoutes = databaseService.getSavedRoutesByUserId(user.getId());


        // 5. 엔티티 리스트를 DTO 리스트로 변환하고 로그 출력 후 반환
        List<SavedRouteDTO> savedRouteDTOs = savedRoutes.stream()
                .map(SavedRouteDTO::fromEntity) // 엔티티를 DTO로 변환
                .collect(Collectors.toList()); // 리스트로 수집

        savedRouteDTOs.forEach(savedRouteDTO -> log.info("Retrieved route: {}", savedRouteDTO));

        return ResponseEntity.ok(savedRouteDTOs); // 200 OK 응답과 함께 DTO 리스트 반환
    }
}
