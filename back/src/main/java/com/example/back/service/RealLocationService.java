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

    // 지하철 도착 정보 (탑승 전)
    protected RealTimeResultDTO processSubwayBoardingData(RealtimeDTO data) throws IOException {
        String[] arrivals;
        String predictTime1String, predictTime2String = null;

        int subwayCode = data.getTransportLocalID();
        int convertedCode = subwayService.convertSubwayCode(subwayCode);
        String startName = data.getStartstationName();
        String endName = data.getEndstationName();
        String direction = data.getDirection();
        String secondStation = data.getSecondstationName();
        // 데이터 형태 구부(데이터베이스, 실시간 데이터
        if (subwayService.DBCode(subwayCode)) {
            arrivals = subwayProcessService.fetchPredictedTimes(subwayCode, startName);
        }else {
            // 실시간 도착정보 처리
            arrivals = subwayProcessService.processSeoulSubway(convertedCode, startName, endName, direction, secondStation);
        }

        predictTime1String = (arrivals.length > 0) ? arrivals[0] : null;
        predictTime1String = (arrivals.length > 1) ? arrivals[1] : null;

        realTimeResultDTO.setPredictTimes1(predictTime1String);
        realTimeResultDTO.setPredictTimes2(predictTime2String);

        return realTimeResultDTO;
    }

    // 지하철 도착 정보 + 실시간 위치(탑승 중)
    protected RealTimeResultDTO processSubwayAlightingData(RealtimeDTO data) throws IOException {

        String trainNm = subwayProcessService.getTranNo(data);
        // 지하철 실시간 위치 정보 가져오기
        RealTimeResultDTO realTimeResultDTO = new RealTimeResultDTO();

        return realTimeResultDTO;

    }
    // 지하철 도착 정보 + 실시간 위치(탑승 중)
    protected RealTimeResultDTO processSubwayAlightedData(RealtimeDTO data) throws IOException {

        String trainNm = subwayProcessService.getTranNo(data);
        // 지하철 실시간 위치 정보 가져오기
        RealTimeResultDTO realTimeResultDTO = new RealTimeResultDTO();

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