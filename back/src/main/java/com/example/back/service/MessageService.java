package com.example.back.service;


import com.example.back.dto.RealTimeResultDTO;
import com.example.back.dto.ResultDTO;
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
public class MessageService {

    @Autowired
    private APIService apiService;

    // 지하철 도착 정보
    protected RealTimeResultDTO processSubwayBoardingData(RealtimeDTO data) throws IOException {

        List<SubwayArriveProcessDTO.RealtimeArrival> realtimeArrivals = apiService.fetchAndSubwayArrive(data.getStationName()).getRealtimeArrivalList();
        RealTimeResultDTO realTimeResultDTO = new RealTimeResultDTO();
        return realTimeResultDTO;
    }

    // 지하철 도착 정보 + 실시간 위치
    protected RealTimeResultDTO processSubwayAlightingData(RealtimeDTO data) throws IOException {
        // 지하철 도착 정보 가져오기
        List<SubwayArriveProcessDTO.RealtimeArrival> subwayArrivals = apiService.fetchAndSubwayArrive(data.getStationName()).getRealtimeArrivalList();
        // 지하철 실시간 위치 정보 가져오기
        RealSubwayLocationDTO subwayLocations = apiService.fetchAndSubwayLocation(data.getStationName());
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
        realTimeResultDTO.setNextRequest(2);
        return realTimeResultDTO;
    }

    // 예외 처리
    protected RealTimeResultDTO processDefaultData(RealtimeDTO data){
        RealTimeResultDTO realTimeResultDTO = new RealTimeResultDTO();
        return realTimeResultDTO;
    }

}
