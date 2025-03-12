package com.example.back.domain;

import java.io.Serializable;
import java.sql.Time;
import java.util.Objects;

public class TimeTablePK implements Serializable {

    private int route_id;
    private String Day_Type;
    private String Station_Name;
    private String Train_ID;
    private Time Arrival_Time;

    // 기본 생성자
    public TimeTablePK() {}

    // Equals & HashCode (복합 키 비교를 위해 필요)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeTablePK that = (TimeTablePK) o;
        return route_id == that.route_id &&
                Objects.equals(Day_Type, that.Day_Type) &&
                Objects.equals(Station_Name, that.Station_Name) &&
                Objects.equals(Train_ID, that.Train_ID) &&
                Objects.equals(Arrival_Time, that.Arrival_Time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(route_id, Day_Type, Station_Name, Train_ID, Arrival_Time);
    }
}
