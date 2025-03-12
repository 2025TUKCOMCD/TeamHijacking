package com.example.back.repository;

import com.example.back.domain.TimeTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import retrofit2.http.Query;

import java.util.List;

@Repository
public interface TimeTableRepository  extends JpaRepository<TimeTable, Long> {
    @Query("SELECT t FROM TimeTable t WHERE t.route_id = :routeId")
    List<TimeTable> findByRouteId(@Param("routeId") int routeId);
}
