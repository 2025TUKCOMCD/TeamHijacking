package com.example.back.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.IdClass;
import lombok.Data;

import java.io.Serializable;
import java.sql.Time;

// 복합 키를 위한 클래스 정의
@Data
@Entity
@Table(name = "TimeTable")
@IdClass(TimeTablePK.class) // 복합 키 클래스 연결
public class TimeTable {

    @Id
    @Column(name = "route_id")
    private int route_id;

    @Id
    @Column(name = "Day_Type")
    private String Day_Type;

    @Id
    @Column(name = "Station_Name")
    private String Station_Name;

    @Id
    @Column(name = "Train_ID")
    private String Train_ID;

    @Id
    @Column(name = "Arrival_Time")
    private Time Arrival_Time;

    @Column(name = "Region")
    private String Region;

    @Column(name = "Direction")
    private String Direction;
}
