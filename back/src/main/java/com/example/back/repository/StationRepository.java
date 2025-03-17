package com.example.back.repository;

import com.example.back.domain.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
    @Query(value = "SELECT s.statn_id FROM station s WHERE s.api_id = :api_id AND s.statn_nm = :statn_nm", nativeQuery = true)
    List<Integer> findStationsByApiIdAndStationName(
            @Param("statn_nm") String statn_nm,
            @Param("api_id") int api_id
    );
}

