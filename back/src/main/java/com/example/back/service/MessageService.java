package com.example.back.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MessageService {

    @Autowired
    private APIService apiService;

//    // 지하철 도착 정보
//    protected  RealTimeResultDTO processSubwayBoardingData(RealtimeDTO data) throws IOException {
//        List<SubwayArriveProcessDTO.RealtimeArrival> realtimeArrivals = null;
//        realtimeArrivals = apiService.fetchAndSubwayArrive(data.getStationName()).getRealtimeArrivalList();
//
//    }
//
//    // 지하철 도착 정보 + 실시간 위치
//    protected RealTimeResultDTO processSubwayAlightingData(RealtimeDTO data) throws IOException {
//        // 지하철 실시간 위치 정보 가져오기
//        List<SubwayArriveProcessDTO.RealtimeArrival> realtimeArrivals = apiService.fetchAndSubwayArrive(data.getStationName()).getRealtimeArrivalList();
//
//        // 지하철 도착 정보 가져오기
//        List<SubwayArriveProcessDTO.RealtimeArrival> subwayArrivals = apiService.fetchAndSubwayArrive(data.getStationName()).getRealtimeArrivalList();
//
//
//    }
//
//    // 버스 도착 정보
//    protected RealTimeResultDTO processBusBoardingData(RealtimeDTO data) throws IOException{
//
//    }
//
//    // 버스 도착 정보 + 실시간 위치
//    protected RealTimeResultDTO processBusAlightingData(RealtimeDTO data) throws IOException{
//        busApi.getBusPosByRouteSt()
//    }
//
//    // 도보의 경우 다음 대중교통 정보 요구 처리
//    protected RealTimeResultDTO processWalkingData(RealtimeDTO data) throws IOException{
//
//    }
//
//    // 예외 처리
//    protected RealTimeResultDTO processDefaultData(RealtimeDTO data){
//
//    }

}
