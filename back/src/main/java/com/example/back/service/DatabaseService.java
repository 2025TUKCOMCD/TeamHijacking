package com.example.back.service;

import com.example.back.domain.TimeTable;
import com.example.back.repository.StationRepository;
import com.example.back.repository.TimeTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.List;

@Service
public class DatabaseService {

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private TimeTableRepository timeTableRepository;

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

}
