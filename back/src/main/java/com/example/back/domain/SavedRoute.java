package com.example.back.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "savedRoute")
public class SavedRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  //Auto Increment 대응 함
    @Column(name = "transportroute_key")
    private Integer transportRouteKey;

    @Column(name = "departure_name", nullable = false, length = 256)
    private String departureName;

    @Column(name = "destination_name", length = 256)
    private String destinationName;

    @Column(name = "when_firstgo", insertable = false)
    private LocalDateTime whenFirstGo;

    @Column(name = "when_lastgo", insertable = false)
    private LocalDateTime whenLastGo;

    @Column(name = "useroute_count", nullable = false)
    private Integer userRouteCount;

    @Column(name = "is_favourite", nullable = false)
    private Boolean isFavourite;

    @Column(name = "start_lat", nullable = false, precision = 13, scale = 10)
    private BigDecimal startLat;

    @Column(name = "start_lng", nullable = false, precision = 13, scale = 10)
    private BigDecimal startLng;

    @Column(name = "end_lat", nullable = false, precision = 13, scale = 10)
    private BigDecimal endLat;

    @Column(name = "end_lng", nullable = false, precision = 13, scale = 10)
    private BigDecimal endLng;

    @Column(name = "savedroute_name", length = 100)
    private String savedRouteName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
