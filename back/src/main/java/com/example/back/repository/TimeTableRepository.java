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
    @Query(value = "SELECT * FROM TimeTable t WHERE t.route_id = :routeId AND t.Station_Name = :stationName AND t.Arrival_Time < :currentTime ORDER BY t.Arrival_Time DESC LIMIT 2", nativeQuery = true)
    List<TimeTable> findTop2PreviousSubwayByRouteIdAndStationName(
            @Param("routeId") int routeId,
            @Param("stationName") String stationName,
            @Param("currentTime") Time currentTime
    );
}

