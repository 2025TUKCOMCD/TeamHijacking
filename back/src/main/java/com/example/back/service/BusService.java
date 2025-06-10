package com.example.back.service;

import com.example.back.dto.bus.detail.BusDetailProcessDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BusService {
    // 정류장 ID 비교
    public List<Integer> compareStationIDs(BusDetailProcessDTO.BusLaneDetail busDetailProcess, int startID, int endID) {
        List<Integer> startIndices = new ArrayList<>();
        List<Integer> endIndices = new ArrayList<>();

        List<BusDetailProcessDTO.Station> stations = busDetailProcess.getResult().getStation();

        if (stations != null) {
            for (BusDetailProcessDTO.Station station : stations) {
                if (station.getStationID() == startID) {
                    startIndices.add(station.getIdx());
                } else if (station.getStationID() == endID) {
                    endIndices.add(station.getIdx());
                }
            }
        }

        List<Integer> stationInfos = new ArrayList<>();

        for (Integer startIndex : startIndices) {
            for (Integer endIndex : endIndices) {
                if (startIndex < endIndex) {
                    stationInfos.add(startIndex + 1);
                    stationInfos.add(endIndex + 1);
                    return stationInfos;
                }
            }
        }

        // 일치하는 정류장 정보 없음
        stationInfos.add(-1);
        return stationInfos;
    }

    // 정류장 실시간 위치 정보 중 현재 버스 특정

}
