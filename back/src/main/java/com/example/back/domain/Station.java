package com.example.back.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "station")
public class Station {
    @Id
    @Column(name = "statn_id") // Primary Key
    private int statn_id;

    @Column(name = "statn_nm")
    private String statn_nm;

    @Column(name = "api_id")
    private int api_id;
}
