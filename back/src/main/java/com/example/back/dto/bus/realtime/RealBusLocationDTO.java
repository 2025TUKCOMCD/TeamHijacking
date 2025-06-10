package com.example.back.dto.bus.realtime;

import lombok.Data;
import java.util.List;

@Data
public class RealBusLocationDTO {
    private ComMsgHeader comMsgHeader;
    private MsgHeader msgHeader;
    private MsgBody msgBody;

    @Data
    public static class ComMsgHeader { // DTO 사용을 위해 static으로 선언
        private String responseTime;
        private String requestMsgID;
        private String responseMsgID;
        private String returnCode;
        private String successYN;
        private String errMsg;
    }

    @Data
    public static class MsgHeader { // static으로 선언
        private String headerMsg;
        private String headerCd;
        private int itemCount; // JSON에서 int 타입이므로 int 유지
    }

    @Data
    public static class MsgBody { // static으로 선언
        private List<ItemList> itemList;
    }

    @Data
    public static class ItemList { // static으로 선언
        private String vehId;       // 차량 ID
        private String stId;        // 정류장 ID (JSON 예시에 포함되어 추가)
        private String stOrd;       // 정류장 순서 (JSON 예시에 포함되어 추가)
        private String stopFlag;    // 정차 여부 (0: 운행 중, 1: 정류장 도착)
        private String dataTm;      // 데이터 생성 시간
        private String tmX;         // TM 좌표 X
        private String tmY;         // TM 좌표 Y
        private String posX;        // 경도 (WGS84)
        private String posY;        // 위도 (WGS84)
        private String plainNo;     // 차량 번호 (예: 서울70사8941)
        private String busType;     // 버스 타입
        private String lastStnId;   // 최종 정류장 ID
        private String isFullFlag;  // 만차 여부
        private String congetion;   // 혼잡도
        // sectOrd, sectDist, sectionId 필드가 JSON 예시에 없으므로 포함하지 않음
        // routeId 필드가 JSON 예시에 없으므로 포함하지 않음
    }
}