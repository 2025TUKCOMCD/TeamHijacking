package com.example.back.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "saved_route")
public class SavedRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  //Auto Increment 대응 함
    @Column(name = "transportroute_key")
    private Integer transportrouteKey;

    @Column(name = "departure_name", nullable = false, length = 256)
    private String departureName;

    @Column(name = "destination_name", length = 256)
    private String destinationName;

    @Column(name = "when_firstgo")
    private LocalDateTime whenFirstGo;

    @Column(name = "when_lastgo")
    private LocalDateTime whenLastGo;

    @Column(name = "useroute_count", nullable = false)
    private Integer userRouteCount;

    @Column(name = "is_favourite", nullable = false)
    private Boolean isFavorite;

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
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private User user;
}
