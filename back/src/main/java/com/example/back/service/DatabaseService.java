package com.example.back.service;

import com.example.back.domain.SavedRoute;
import com.example.back.domain.TimeTable;
import com.example.back.domain.User;
import com.example.back.dto.SavedRouteResponseDTO;
import com.example.back.dto.UserDTO;
import com.example.back.dto.route.SavedRouteDTO;
import com.example.back.repository.SavedRouteRepository;
import com.example.back.repository.StationRepository;
import com.example.back.repository.TimeTableRepository;
import com.example.back.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DatabaseService {

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private SavedRouteRepository savedRouteRepository;

    @Autowired
    private TimeTableRepository timeTableRepository;
    @Autowired
    private UserRepository userRepository;

    public int findStationId(int convertedSubwayCode, String startName ) {
        // 데이터베이스에서 statn_id 조회
        List<Integer> result = stationRepository.findStationsByApiIdAndStationName(startName, convertedSubwayCode);

        // statn_id 추출 및 로직 작성
        int statn_id = result.get(0); // 첫 번째 결과 가져오기
        return statn_id;
    }

    public List<TimeTable> findTrainNoSubwayArrivals(int subwayCode, String startName, Time seoulTime ,String currentDayType, String TrainNo, String direction) {
        System.out.println("Querying TimeTable with:");
        System.out.println("  routeId: " + subwayCode);
        System.out.println("  stationName: " + startName);
        System.out.println("  currentTime: " + seoulTime);
        System.out.println("  TrainNo: " + TrainNo);
        System.out.println("  dayType: " + currentDayType);

        // Repository 호출하여 현재 시간 이후의 도착 정보를 가져오기
        List<TimeTable> result = timeTableRepository.findTrainNoSubwayByRouteIdAndStationNameAndDayTypeAndDirection(
                subwayCode, startName, seoulTime, currentDayType, TrainNo, direction
        );
        System.out.println("DatabaseService - findTrainNoSubwayArrivals: " + result);
        return result;
    }

    public List<TimeTable> findNextSubwayArrivals(int subwayCode, String startName, Time seoulTime, String currentDayType, String direction) {
        System.out.println("Querying TimeTable with:");
        System.out.println("  routeId: " + subwayCode);
        System.out.println("  stationName: " + startName);
        System.out.println("  currentTime: " + seoulTime);
        System.out.println("  dayType: " + currentDayType);
        System.out.println("  direction: " + direction);

        // Repository 호출하여 현재 시간 이후의 도착 정보를 가져오기
        List<TimeTable> result = timeTableRepository.findNextSubwayByRouteIdAndStationNameAndDayTypeAndDirection(
                subwayCode, startName, seoulTime, currentDayType, direction
        );
        System.out.println("DatabaseService - findNextSubwayArrivals: " + result);
        return result;
    }

    @Transactional // 트랜잭션 관리
    public SavedRouteResponseDTO updateRoute(SavedRouteDTO savedRouteDTO, Integer userId) {
        try {
            // userId로 User 엔티티를 찾습니다.
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

            // transroute_key와 User 엔티티를 사용하여 기존 SavedRoute를 찾습니다.
            // DTO에서 transrouteKey를 받아와야 합니다.
            Integer transrouteKeyToUpdate = savedRouteDTO.getTransportrouteKey();
            if (transrouteKeyToUpdate == null || transrouteKeyToUpdate == 0) {
                return SavedRouteResponseDTO.builder()
                        .success(false)
                        .message("업데이트할 경로의 'transrouteKey'가 유효하지 않습니다.")
                        .build();
            }

            Optional<SavedRoute> existingRouteOptional = savedRouteRepository.findByTransportrouteKeyAndUser(
                    transrouteKeyToUpdate, user);

            if (existingRouteOptional.isEmpty()) {
                System.out.println("Route not found for update with transrouteKey:"+ transrouteKeyToUpdate +"userId : "+ userId);
                return SavedRouteResponseDTO.builder()
                        .success(false)
                        .message("업데이트할 경로를 찾을 수 없습니다. (Key: " + transrouteKeyToUpdate + ")")
                        .build();
            }

            // 기존 경로 엔티티를 가져와 업데이트합니다.
            SavedRoute existingRoute = existingRouteOptional.get();

            // DTO의 필드로 엔티티 업데이트
            existingRoute.setDepartureName(savedRouteDTO.getDepartureName());
            existingRoute.setDestinationName(savedRouteDTO.getDestinationName()); // destinationName으로 수정
            existingRoute.setStartLat(savedRouteDTO.getStartLat());
            existingRoute.setStartLng(savedRouteDTO.getStartLng());
            existingRoute.setEndLat(savedRouteDTO.getEndLat());
            existingRoute.setEndLng(savedRouteDTO.getEndLng());
            existingRoute.setSavedRouteName(savedRouteDTO.getSavedRouteName());
            existingRoute.setIsFavorite(savedRouteDTO.getIsFavorite()); // 즐겨찾기 상태 업데이트

            // userRouteCount를 1 증가시키고 whenLastGo를 현재 시간으로 업데이트
            existingRoute.setUserRouteCount((existingRoute.getUserRouteCount() != null ? existingRoute.getUserRouteCount() : 0) + 1);
            existingRoute.setWhenLastGo(LocalDateTime.now());


            // DB에 변경사항 저장 (JPA Dirty Checking으로 인해 save 호출 없이도 트랜잭션 종료 시 자동 반영될 수 있으나, 명시적으로 호출)
            SavedRoute updatedRoute = savedRouteRepository.save(existingRoute);

            System.out.println("Route updated : " + updatedRoute);
            return SavedRouteResponseDTO.builder()
                    .success(true)
                    .message("경로가 성공적으로 업데이트되었습니다.")
                    .build();

        } catch (Exception e) {
            System.out.println("Failed to update route : "+ e);
            return SavedRouteResponseDTO.builder()
                    .success(false)
                    .message("경로 업데이트에 실패했습니다: " + e.getMessage())
                    .build();
        }
    }

    @Transactional
    public SavedRouteResponseDTO saveRoute(SavedRouteDTO savedRouteDTO, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // 기존 경로 확인
        Optional<SavedRoute> existingRouteOptional = savedRouteRepository.findByDepartureNameAndDestinationNameAndStartLatAndStartLngAndEndLatAndEndLngAndUser_LoginId(
                savedRouteDTO.getDepartureName(),
                savedRouteDTO.getDestinationName(),
                savedRouteDTO.getStartLat(),
                savedRouteDTO.getStartLng(),
                savedRouteDTO.getEndLat(),
                savedRouteDTO.getEndLng(),
                user.getLoginId()
        );

        if (existingRouteOptional.isPresent()) {
            // 3. 이미 동일한 경로가 존재하는 경우
            SavedRoute existingRoute = existingRouteOptional.get();
            System.out.println("Route already exists with transportRouteKey: " + existingRoute.getTransportrouteKey());

            // userRouteCount 증가
            existingRoute.setUserRouteCount((existingRoute.getUserRouteCount() != null ? existingRoute.getUserRouteCount() : 0) + 1); // null 체크 추가
            // isFavorite 업데이트 (DTO에서 값이 제공되면 업데이트)
            if (savedRouteDTO.getIsFavorite() != null) {
                existingRoute.setIsFavorite(savedRouteDTO.getIsFavorite());
            }
            // whenLastGo를 현재 시간으로 업데이트
            existingRoute.setWhenLastGo(LocalDateTime.now()); // <--- 여기 업데이트
            savedRouteRepository.save(existingRoute); // 변경 사항 저장

            return SavedRouteResponseDTO.builder()
                    .success(true)
                    .message("이미 존재하는 경로입니다. 사용 횟수가 업데이트되었습니다.")
                    .savedRouteName(existingRoute.getSavedRouteName())
                    .alreadyExists(true)
                    .build();
        } else {
            // 4. 경로가 존재하지 않는 경우, 새로 저장합니다.
            // DTO를 엔티티로 변환할 때 User 객체 연결
            SavedRoute newRoute = SavedRoute.builder()
                    .departureName(savedRouteDTO.getDepartureName())
                    .destinationName(savedRouteDTO.getDestinationName())
                    .startLat(savedRouteDTO.getStartLat())
                    .startLng(savedRouteDTO.getStartLng())
                    .endLat(savedRouteDTO.getEndLat())
                    .endLng(savedRouteDTO.getEndLng())
                    .savedRouteName(savedRouteDTO.getSavedRouteName())
                    .user(user) // User 객체 연결
                    .build();

            // isFavorite이 DTO에서 null이면 기본값 설정
            if (newRoute.getIsFavorite() == null) {
                newRoute.setIsFavorite(false);
            }
            // savedRouteName이 null/비어 있으면 기본값 설정
            if (newRoute.getSavedRouteName() == null || newRoute.getSavedRouteName().isEmpty()) {
                newRoute.setSavedRouteName("비어있는 경로");
            }
            // userRouteCount 초기화
            newRoute.setUserRouteCount(1);
            // whenFirstGo와 whenLastGo를 현재 시간으로 설정
            newRoute.setWhenFirstGo(LocalDateTime.now()); // <--- 여기 초기 설정
            newRoute.setWhenLastGo(LocalDateTime.now()); // <--- 여기 초기 설정

            SavedRoute saved = savedRouteRepository.save(newRoute);
            System.out.println("New route saved successfully with transportRouteKey: " + saved.getTransportrouteKey());

            return SavedRouteResponseDTO.builder()
                    .success(true)
                    .message("새로운 경로가 성공적으로 저장되었습니다.")
                    .savedRouteName(saved.getSavedRouteName())
                    .alreadyExists(false)
                    .build();
        }
    }

    // 데이터베이스 userId를 통해 SavedRoute 테이블 가져오기
    public List<SavedRoute> getSavedRoutesByUserId(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // 2. 찾은 User 객체를 사용하여 SavedRoute 목록을 조회합니다.
        return savedRouteRepository.findByUser_LoginId(
                user.getLoginId()
        );
    }

    // 데이터베이스 loginId를 통해 user테이블 userId 가져오기
    @Transactional(readOnly = true)
    public Optional<User> findUserByLoginId(String loginId) { // 메서드명 변경: findUserId -> findUserByLoginId
        System.out.println("Finding user by loginId: " + loginId);
        return userRepository.findByLoginId(loginId);
    }
}
