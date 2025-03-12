package com.example.back.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.sql.Time;

@Data
@Entity
@Table(name = "TimeTable")
public class TimeTable {

    private int route_id;
    private String Region;
    private String Station_Name;
    private Time Arrival_Time;
    private String Day_Type;
    private String Train_ID;
    private String Direction;
}
