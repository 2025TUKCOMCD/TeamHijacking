package com.example.back.dto.bus.realtime;
import lombok.Data;
import java.util.List;

@Data
public class RealBusLocationDTO {
    private ComMsgHeader comMsgHeader;
    private MsgHeader msgHeader;
    private MsgBody msgBody;

    @Data
    public class ComMsgHeader {
        private String errMsg;
        private String responseTime;
        private String requestMsgID;
        private String responseMsgID;
        private String successYN;
        private String returnCode;
    }

    @Data
    public class MsgHeader {
        private String headerMsg;
        private String headerCd;
        private int itemCount;
    }

    @Data
    public class MsgBody {
        private List<ItemList> itemList;
    }

    @Data
    public class ItemList {
        private String sectOrd;
        private String sectDist;
        private String stopFlag;
        private String sectionId;
        private String dataTm;
        private String tmX;
        private String tmY;
        private String posX;
        private String posY;
        private String vehId;
        private String plainNo;
        private String busType;
        private String routeId;
        private String lastStnId;
        private String isFullFlag;
        private String congetion;
    }
}