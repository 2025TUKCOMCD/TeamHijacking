package com.example.back.service;

import com.example.back.dto.RealTimeResultDTO;
import com.example.back.dto.bus.arrive.BusArriveProcessDTO;
import com.example.back.dto.bus.realtime.RealBusLocationDTO;
import com.example.back.dto.realtime.RealtimeDTO;
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
                }
                else if(data.getBoarding()==2){// 탑승 중 : trainNm 추출 및 현재 위치 추출
                    try{
                        return processSubwayAlightingData(data);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                else if(data.getBoarding()==3){ // 탑승 후 : 실시간 위치 정보
                    try{
                        return processSubwayAlightedData(data);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            case 2: // 버스 도착 정보
                if(data.getBoarding() == 1 ) { // 탑승 전 : 실시간 도착 정보
                    try {
                        return processBusBoardingData(data);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }else if(data.getBoarding() == 2) {// 탑승 중 : 실시간 위치 정보
                    try {
                        return processBusAlightingData(data);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }else if(data.getBoarding() == 3){
                    // 탑승 후 : 실시간 위치 정보
                    try {
                        return processBusAlightedData(data);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
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
        RealTimeResultDTO realTimeResultDTO = new RealTimeResultDTO();
        String predictTime1String = "도착 정보 없음";
        String predictTime2String = "도착 정보 없음";

        int subwayCode = data.getTransportLocalID();
        String startName = data.getStartName();

        String[] arrivals = subwayProcessService.fetchPredictedTimes(subwayCode, startName);

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

    // 지하철 도착 정보 (탑승 전)
    protected RealTimeResultDTO processSubwayBoardingData(RealtimeDTO data) throws IOException {
        RealTimeResultDTO realTimeResultDTO = new RealTimeResultDTO();
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
        RealTimeResultDTO realTimeResultDTO = new RealTimeResultDTO();
        int trainNm = subwayProcessService.getTranNo(data);
        // 지하철 실시간 위치 정보 가져오기
        realTimeResultDTO.setTrainNo(trainNm);
        return realTimeResultDTO;

    }

    // 지하철 도착 정보 + 실시간 위치(탑승 후)
    protected RealTimeResultDTO processSubwayAlightedData(RealtimeDTO data) throws IOException {
        RealTimeResultDTO realTimeResultDTO = new RealTimeResultDTO();
        String[] arrivals;
        String predictTime1String = "도착 정보 없음"; // 기본값으로 초기화

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
        return realTimeResultDTO;
    }

    // 버스 도착 정보 (탑승 전)
    protected RealTimeResultDTO processBusBoardingData(RealtimeDTO data) throws IOException{
        // 버스 도착 정보 가져오기
        RealTimeResultDTO realTimeResultDTO = new RealTimeResultDTO();
        String predictTime1String = "도착 정보 없음";
        String predictTime2String = "도착 정보 없음";

        BusArriveProcessDTO.arriveDetail busArrivals = apiService.fetchAndBusArrive(data.getStationId(),data.getTransportLocalID(),data.getStartOrd());
        String vehId = busArrivals.getMsgBody().getItemList().get(0).getVehId1();
        System.out.println("Bus Arrivals: " + busArrivals);
        if (busArrivals != null && busArrivals.getMsgBody() != null && busArrivals.getMsgBody().getItemList() != null) {
            List<BusArriveProcessDTO.Item> itemList = busArrivals.getMsgBody().getItemList();
            if (!itemList.isEmpty()) {
                System.out.println("Item List: " + itemList);
                predictTime1String = itemList.get(0).getArrmsg1(); // 첫 번째 예측 시간
                predictTime2String = itemList.get(0).getArrmsg2(); // 두 번째 예측 시간
            }
        }
        realTimeResultDTO.setNextRequest(1); // 다음 대중교통 정보 요청
        realTimeResultDTO.setVehId(vehId);
        realTimeResultDTO.setPredictTimes1(predictTime1String);
        realTimeResultDTO.setPredictTimes2(predictTime2String);
        return realTimeResultDTO;
    }

    // 버스 도착 정보 + 실시간 위치(탑승 중)
    private RealTimeResultDTO processBusAlightingData(RealtimeDTO data) throws IOException{
        RealTimeResultDTO realTimeResultDTO = new RealTimeResultDTO();
        // 버스 도착 정보 가져오기
        RealBusLocationDTO busLocations = apiService.fetchAndBusLocation(data.getVehid());
        String stOrd= busLocations.getMsgBody().getItemList().get(0).getStOrd();

        realTimeResultDTO.setNextRequest(1); // 다음 대중교통 정보 요청
        realTimeResultDTO.setVehId(data.getVehid()); // 버스 차량 ID
        realTimeResultDTO.setLocation(stOrd); // 현재 위치 정보
        return realTimeResultDTO;
    }

    // 버스 도착 정보 + 실시간 위치(탑승 후)
    protected RealTimeResultDTO processBusAlightedData(RealtimeDTO data) throws IOException{
        RealTimeResultDTO realTimeResultDTO = new RealTimeResultDTO();
        RealBusLocationDTO busLocations = apiService.fetchAndBusLocation(data.getVehid());
        // 현재 버스 위치 기반 정류장 도착 정보 처리
        String stOrd= busLocations.getMsgBody().getItemList().get(0).getStOrd();
        realTimeResultDTO.setLocation(stOrd); // 현재 위치 정보
        return realTimeResultDTO;
    }

    // 도보의 경우 다음 대중교통 정보 요구 처리
    protected RealTimeResultDTO processWalkingData(RealtimeDTO data) throws IOException{
        RealTimeResultDTO realTimeResultDTO = new RealTimeResultDTO();
        realTimeResultDTO.setNextRequest(2); // 다음 대중교통 정보 요청
        return realTimeResultDTO;
    }

    // 예외 처리
    protected RealTimeResultDTO processDefaultData(RealtimeDTO data){
        RealTimeResultDTO realTimeResultDTO = new RealTimeResultDTO();
        return realTimeResultDTO;
    }
}