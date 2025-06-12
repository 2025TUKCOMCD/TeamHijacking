package com.example.back.dto.route;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class SavedRouteDTO {
    private Integer transportRouteKey;
    private String departureName;
    private String destinationName;
    private LocalDateTime whenFirstGo;
    private LocalDateTime whenLastGo;
    private Integer useRouteCount;
    private Boolean isFavourite;
    private BigDecimal startLat;
    private BigDecimal startLng;
    private BigDecimal endLat;
    private BigDecimal endLng;
    private String savedRouteName;
    private Integer userId;
}
