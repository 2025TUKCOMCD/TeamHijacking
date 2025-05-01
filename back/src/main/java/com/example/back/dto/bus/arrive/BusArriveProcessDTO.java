package com.example.back.dto.bus.arrive;

import lombok.Data;

import java.util.List;
@Data
public class BusArriveProcessDTO {

    @Data
    public static class arriveDetail {
        private MsgBody msgBody;
    }

    @Data
    public static class MsgBody {
        private List<Item> itemList;
    }

    @Data
    public static class Item {
        private String arrmsg1; // 첫 번째 도착 메시지
        private String arrmsg2; // 두 번째 도착 메시지
    }
}
