package com.example.back.repository;

import com.example.back.domain.TimeTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface TimeTableRepository extends JpaRepository<TimeTable, Long> {
    @Query(value = "SELECT * FROM TimeTable t " + "WHERE t.route_id = :routeId " +
            "AND t.Station_Name = :stationName " + "AND t.Arrival_Time > :currentTime " +
            "AND t.Day_Type = :dayType " +"AND t.Direction = :direction " +
            "ORDER BY t.Arrival_Time ASC " + "LIMIT 2", nativeQuery = true)
    List<TimeTable> findNextSubwayByRouteIdAndStationNameAndDayTypeAndDirection( // 메서드명 변경 (direction 포함)
         @Param("routeId") int routeId,
         @Param("stationName") String stationName,
         @Param("currentTime") Time currentDayTime,
         @Param("dayType") String dayType,
         @Param("direction") String direction
    );

    @Query(value = "SELECT * FROM TimeTable t " +
            "WHERE t.route_id = :routeId " +
            "AND t.Station_Name = :stationName " +
            "AND t.Arrival_Time > :currentTime " +
            "AND t.Day_Type = :dayType " +
            "AND t.Train_ID = :TrainNo " +
            "ORDER BY t.Arrival_Time ASC " +
            "LIMIT 2", nativeQuery = true)
    List<TimeTable> findTrainNoSubwayByRouteIdAndStationNameAndDayTypeAndDirection(
            @Param("routeId") int routeId,
            @Param("stationName") String stationName,
            @Param("currentTime") Time currentDayTime,
            @Param("dayType") String dayType,
            @Param("TrainNo") String TrainNo,
            @Param("direction") String direction
    );
}
