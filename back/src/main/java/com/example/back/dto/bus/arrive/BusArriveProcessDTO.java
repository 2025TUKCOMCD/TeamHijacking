package com.example.back.dto.bus.arrive;

import lombok.Data;

import java.util.List;

@Data
public class BusArriveProcessDTO {
    @Data
    public static class arriveDetail{
        private MsgBody msgBody;
    }

    @Data
    public static class MsgBody {
        private List<Item> itemList;
    }

    @Data
    public static class Item {
        private String stNm;
        private String busRouteAbrv;
        private String rtNm;
        private String plainNo1;
        private String traTime1;
        private String isArrive1;
        private String arrmsg1;
        private String plainNo2;
        private String traTime2;
        private String isArrive2;
        private String arrmsg2;
    }
}
