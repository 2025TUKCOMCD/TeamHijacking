package com.example.back.service;

import com.example.back.dto.RealTimeResultDTO;
import com.example.back.dto.bus.arrive.BusArriveProcessDTO;
import com.example.back.dto.bus.realtime.RealBusLocationDTO;
import com.example.back.dto.realtime.RealtimeDTO;
import com.example.back.dto.subway.arrive.SubwayArriveProcessDTO;
import com.example.back.dto.subway.realtime.RealSubwayLocationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class RealLocationService {

    @Autowired
    private APIService apiService;

    @Autowired
    private SubwayService subwayService;

    @Autowired
    private SubwayProcessService subwayProcessService;

    RealTimeResultDTO realTimeResultDTO = new RealTimeResultDTO();

    // Type, Boarding 여부에 따라 처리
    public RealTimeResultDTO getRealTime(RealtimeDTO data){
        int type = data.getType();
        switch (type){
            case 1: // 지하철 도착 정보
                if(data.getDBUsage()==1){
                    return DBSubwayData(data);
                } else{
                if(data.getBoarding() == 1) { // 탑승 전 : 실시간 도착 정보
                    try {
                        return processSubwayBoardingData(data);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else if(data.getBoarding()==2){ // 탑승 후 : 실시간 위치 정보
                    try{
                        return processSubwayAlightedData(data);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } // 탑승 중 : trainNm 추출 및 현재 위치 추출
                try{
                    return processSubwayAlightingData(data);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case 2: // 버스 도착 정보
                if(data.getBoarding() ==1 ) { // 탑승 전 : 실시간 도착 정보
                    try {
                        return processBusBoardingData(data);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } // 탑승 후 : 실시간 도착 정보
                try {
                    return processBusAlightingData(data);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            case 3: // 도보 도착정보
                try {
                    return processWalkingData(data);
                }catch (IOException e) {
                    throw new RuntimeException(e);
                }
            default:
                return processDefaultData(data);
        }
    }

    // DB에서 지하철 도착 정보 가져오기
    private RealTimeResultDTO DBSubwayData(RealtimeDTO data) {
        int subwayCode = data.getTransportLocalID();
        String startName = data.getStartName();
        String predictTime1String, predictTime2String = null;
        String[] arrivals = subwayProcessService.fetchPredictedTimes(subwayCode, startName);
        predictTime1String = (arrivals.length > 0) ? arrivals[0] : null;
        predictTime1String = (arrivals.length > 1) ? arrivals[1] : null;
        realTimeResultDTO.setNextRequest(1);
        realTimeResultDTO.setPredictTimes1(predictTime1String);
        realTimeResultDTO.setPredictTimes2(predictTime2String);
        return realTimeResultDTO;
    }

    // 지하철 도착 정보 (탑승 전)
    protected RealTimeResultDTO processSubwayBoardingData(RealtimeDTO data) throws IOException {
        String[] arrivals;
        String predictTime1String = "도착 정보 없음";
        String predictTime2String = "도착 정보 없음";

        int subwayCode = data.getTransportLocalID();
        int convertedCode = subwayService.convertSubwayCode(subwayCode);
        String startName = data.getStartName();
        String endName = data.getEndName();
        String direction = data.getDirection();
        String secondName = data.getSecondName();

        // 실시간 도착정보 처리
        arrivals = subwayProcessService.processSeoulSubway(convertedCode, startName, endName, direction, secondName);

        if (arrivals != null && arrivals.length > 0) {
            predictTime1String = arrivals[0]; // predictTime1에 첫 번째 할당
        }
        if (arrivals != null && arrivals.length > 1) {
            predictTime2String = arrivals[1]; // predictTime2에 두 번째 할당
        }
        realTimeResultDTO.setNextRequest(1);
        realTimeResultDTO.setPredictTimes1(predictTime1String);
        realTimeResultDTO.setPredictTimes2(predictTime2String);

        return realTimeResultDTO;
    }

    // 지하철 도착 정보 + 실시간 위치(탑승 중)
    protected RealTimeResultDTO processSubwayAlightingData(RealtimeDTO data) throws IOException {
        int trainNm = subwayProcessService.getTranNo(data);
        // 지하철 실시간 위치 정보 가져오기
        realTimeResultDTO.setTrainNo(trainNm);
        return realTimeResultDTO;

    }

    // 지하철 도착 정보 + 실시간 위치(탑승 후)
    protected RealTimeResultDTO processSubwayAlightedData(RealtimeDTO data) throws IOException {

        String[] arrivals;
        String predictTime1String = "도착 정보 없음"; // 기본값으로 초기화
        String predictTime2String = "도착 정보 없음"; // 기본값으로 초기화

        int subwayCode = data.getTransportLocalID();
        int convertedCode = subwayService.convertSubwayCode(subwayCode);
        String startName = data.getStartName();
        String endName = data.getEndName();
        String direction = data.getDirection();
        String secondStation = data.getSecondName();

        // 실시간 도착정보 처리
        arrivals = subwayProcessService.processSeoulSubway(convertedCode, startName, endName, direction, secondStation);

        String location = subwayProcessService.getLocation(data);

        if (arrivals != null && arrivals.length > 0) {
            predictTime1String = arrivals[0]; // 첫 번째 예측 시간 할당
        }

        if (arrivals != null && arrivals.length > 1) { // 배열에 두 번째 요소가 있는지 확인
            predictTime2String = arrivals[1]; // 두 번째 예측 시간 할당
        }

        String endstationName= data.getEndName();
        // 지하철 실시간 위치 정보 가져오기
        System.out.println("Location: " + location);


        if(location!= null && location.equals(endstationName)){
            realTimeResultDTO.setNextRequest(2);
        } else { // nextRequest가 항상 1이 되도록 명시적 설정
            realTimeResultDTO.setNextRequest(1);
        }


        realTimeResultDTO.setLocation(location);
        realTimeResultDTO.setPredictTimes1(predictTime1String);
        realTimeResultDTO.setPredictTimes2(predictTime2String);
        return realTimeResultDTO;
    }

    // 버스 도착 정보
    protected RealTimeResultDTO processBusBoardingData(RealtimeDTO data) throws IOException{
        // 버스 도착 정보 가져오기
        BusArriveProcessDTO.arriveDetail busArrivals = apiService.fetchAndBusArrive(data.getTransportLocalID(),data.getStationId(),data.getStartOrd());
        RealTimeResultDTO realTimeResultDTO = new RealTimeResultDTO();
        return realTimeResultDTO;
    }

    // 버스 도착 정보 + 실시간 위치
    protected RealTimeResultDTO processBusAlightingData(RealtimeDTO data) throws IOException{
        BusArriveProcessDTO.arriveDetail busArrivals = apiService.fetchAndBusArrive(data.getTransportLocalID(),data.getStationId(),data.getStartOrd());
        RealBusLocationDTO busLocations = apiService.fetchAndBusLocation(data.getStationId(),data.getStartOrd(),data.getEndOrd());
        RealTimeResultDTO realTimeResultDTO = new RealTimeResultDTO();

        return realTimeResultDTO;
    }

    // 도보의 경우 다음 대중교통 정보 요구 처리
    protected RealTimeResultDTO processWalkingData(RealtimeDTO data) throws IOException{
        RealTimeResultDTO realTimeResultDTO = new RealTimeResultDTO();
        realTimeResultDTO.setNextRequest(1); // 다음 대중교통 정보 요청
        return realTimeResultDTO;
    }

    // 예외 처리
    protected RealTimeResultDTO processDefaultData(RealtimeDTO data){
        RealTimeResultDTO realTimeResultDTO = new RealTimeResultDTO();
        return realTimeResultDTO;
    }
}