package com.example.back.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RouteTimeService {

    // 역 연결 및 시간 구성
    private static Map<Integer, Map<String, Integer>> stationTravelTime;

    public RouteTimeService() {
       stationTravelTime = new HashMap<>();
    }

    // 시간 초기화
    @PostConstruct
    private void initializeTravelTime() {
        Map<String, Integer> station1TravelTime = new HashMap<>();
        Map<String, Integer> station4TravelTime = new HashMap<>();
        Map<String, Integer> station8TravelTime = new HashMap<>();
        Map<String, Integer> stationARTravelTime = new HashMap<>();
        Map<String, Integer> stationGCLTravelTime = new HashMap<>();
        Map<String, Integer> stationGLTravelTime = new HashMap<>();
        Map<String, Integer> stationSBLTravelTime = new HashMap<>();
        Map<String, Integer> stationSLTravelTime = new HashMap<>();
        Map<String, Integer> stationGGLTravelTime = new HashMap<>();
        Map<String, Integer> stationSHLTravelTime = new HashMap<>();
        Map<String, Integer> stationSLLTravelTime = new HashMap<>();
        Map<String, Integer> stationGTXTravelTime = new HashMap<>();



        //  메인 노선 (연천 ~ 구로)
        station1TravelTime.put("연천-전곡", 8);
        station1TravelTime.put("전곡-청산", 5);
        station1TravelTime.put("청산-소요산", 6);
        station1TravelTime.put("소요산-동두천", 4);
        station1TravelTime.put("동두천-보산", 2);
        station1TravelTime.put("보산-동두천중앙", 3);
        station1TravelTime.put("동두천중앙-지행", 2);
        station1TravelTime.put("지행-덕정", 5);
        station1TravelTime.put("덕정-덕계", 3);
        station1TravelTime.put("덕계-양주", 5);
        station1TravelTime.put("양주-녹양", 3);
        station1TravelTime.put("녹양-가능", 2);
        station1TravelTime.put("가능-의정부", 2);
        station1TravelTime.put("의정부-회룡", 3);
        station1TravelTime.put("회룡-망월사", 3);
        station1TravelTime.put("망월사-도봉산", 3);
        station1TravelTime.put("도봉산-도봉", 2);
        station1TravelTime.put("도봉-방학", 2);
        station1TravelTime.put("방학-창동", 2);
        station1TravelTime.put("창동-녹천", 2);
        station1TravelTime.put("녹천-월계", 3);
        station1TravelTime.put("월계-광운대", 3);
        station1TravelTime.put("광운대-석계", 2);
        station1TravelTime.put("석계-신이문", 3);
        station1TravelTime.put("신이문-외대앞", 1);
        station1TravelTime.put("외대앞-회기", 2);
        station1TravelTime.put("회기-청량리", 3);
        station1TravelTime.put("청량리-제기동", 2);
        station1TravelTime.put("제기동-신설동", 2);
        station1TravelTime.put("신설동-동묘앞", 2);
        station1TravelTime.put("동묘앞-동대문", 2);
        station1TravelTime.put("동대문-종로5가", 2);
        station1TravelTime.put("종로5가-종로3가", 2);
        station1TravelTime.put("종로3가-종각", 2);
        station1TravelTime.put("종각-시청", 3);
        station1TravelTime.put("시청-서울", 2);
        station1TravelTime.put("서울-남영", 3);
        station1TravelTime.put("남영-용산", 3);
        station1TravelTime.put("용산-노량진", 3);
        station1TravelTime.put("노량진-대방", 3);
        station1TravelTime.put("대방-신길", 1);
        station1TravelTime.put("신길-영등포", 3);
        station1TravelTime.put("영등포-신도림", 2);
        station1TravelTime.put("신도림-구로", 3);

        //  분기 1: 구로 → 인천 (구일~인천)
        station1TravelTime.put("구로-구일", 2);
        station1TravelTime.put("구일-개봉", 2);
        station1TravelTime.put("개봉-오류동", 2);
        station1TravelTime.put("오류동-온수", 3);
        station1TravelTime.put("온수-역곡", 4);
        station1TravelTime.put("역곡-소사", 3);
        station1TravelTime.put("소사-부천", 3);
        station1TravelTime.put("부천-중동", 3);
        station1TravelTime.put("중동-송내", 3);
        station1TravelTime.put("송내-부개", 2);
        station1TravelTime.put("부개-부평", 3);
        station1TravelTime.put("부평-백운", 2);
        station1TravelTime.put("백운-동암", 2);
        station1TravelTime.put("동암-간석", 2);
        station1TravelTime.put("간석-주안", 3);
        station1TravelTime.put("주안-도화", 2);
        station1TravelTime.put("도화-제물포", 2);
        station1TravelTime.put("제물포-도원", 2);
        station1TravelTime.put("도원-동인천", 2);
        station1TravelTime.put("동인천-인천", 2);

        // ️ 분기 2: 구로 → 금천구청 → 병점
        station1TravelTime.put("구로-가산디지털단지", 4);
        station1TravelTime.put("가산디지털단지-독산", 3);
        station1TravelTime.put("독산-금천구청", 2);
        station1TravelTime.put("금천구청-석수", 3);
        station1TravelTime.put("석수-관악", 3);
        station1TravelTime.put("관악-안양", 3);
        station1TravelTime.put("안양-명학", 3);
        station1TravelTime.put("명학-금정", 2);
        station1TravelTime.put("금정-군포", 3);
        station1TravelTime.put("군포-당정", 2);
        station1TravelTime.put("당정-의왕", 3);
        station1TravelTime.put("의왕-성균관대", 3);
        station1TravelTime.put("성균관대-화서", 3);
        station1TravelTime.put("화서-수원", 3);
        station1TravelTime.put("수원-세류", 4);
        station1TravelTime.put("세류-병점", 4);

        // 분기 2-1: 구로 → 광명
        station1TravelTime.put("금천구청-광명", 5);

        // 분기 2-2: 병점 → 서동탄
        station1TravelTime.put("병점-서동탄", 6);
        // 분기 2-3: 병점 → 신창
        station1TravelTime.put("병점-세마", 3);
        station1TravelTime.put("세마-오산대", 3);
        station1TravelTime.put("오산대-오산", 4);
        station1TravelTime.put("오산-진위", 3);
        station1TravelTime.put("진위-송탄", 4);
        station1TravelTime.put("송탄-서정리", 3);
        station1TravelTime.put("서정리-지제", 4);
        station1TravelTime.put("지제-평택", 4);
        station1TravelTime.put("평택-성환", 4);
        station1TravelTime.put("성환-직산", 3);
        station1TravelTime.put("직산-두정", 5);
        station1TravelTime.put("두정-천안", 5);
        station1TravelTime.put("천안-봉명", 2);
        station1TravelTime.put("봉명-쌍용(나사렛대)", 3);
        station1TravelTime.put("쌍용(나사렛대)-아산", 2);
        station1TravelTime.put("아산-탕정", 3);
        station1TravelTime.put("탕정-배방", 4);
        station1TravelTime.put("배방-온양온천", 4);
        station1TravelTime.put("온양온천-신창", 5);
        stationTravelTime.put(1001, station1TravelTime);

        // 메인 노선 (오이도 ~ 불암산)
        station4TravelTime.put("오이도-정왕",3);
        station4TravelTime.put("정왕-신길온천",3);
        station4TravelTime.put("신길온천-안산",3);
        station4TravelTime.put("안산-초지",3);
        station4TravelTime.put("초지-고잔",2);
        station4TravelTime.put("고잔-중앙",2);
        station4TravelTime.put("중앙-한대앞",3);
        station4TravelTime.put("한대앞-상록수",2);
        station4TravelTime.put("상록수-반월",3);
        station4TravelTime.put("반월-대야미",3);
        station4TravelTime.put("대야미-수리산",3);
        station4TravelTime.put("수리산-산본",2);
        station4TravelTime.put("산본-금정",5);
        station4TravelTime.put("금정-범계",3);
        station4TravelTime.put("범계-평촌",2);
        station4TravelTime.put("평촌-인덕원",3);
        station4TravelTime.put("인덕원-정부과천청사",3);
        station4TravelTime.put("정부과천청사-과천",2);
        station4TravelTime.put("과천-대공원",2);
        station4TravelTime.put("대공원-경마공원",2);
        station4TravelTime.put("경마공원-선바위",2);
        station4TravelTime.put("선바위-남태령",3);
        station4TravelTime.put("남태령-사당",3);
        station4TravelTime.put("사당-총신대입구(이수)",2); // 총신대입구랑 이수역 이름이 두개인데 뭐로 해야함?
        station4TravelTime.put("총신대입구-동작",3);
        station4TravelTime.put("동작-이촌",3);
        station4TravelTime.put("이촌-신용산",3);
        station4TravelTime.put("신용산-삼각지",1);
        station4TravelTime.put("삼각지-숙대입구",2);
        station4TravelTime.put("숙대입구-서울",2);
        station4TravelTime.put("서울-회현",2);
        station4TravelTime.put("회현-명동",2);
        station4TravelTime.put("명동-충무로",1);
        station4TravelTime.put("충무로-동대문역사문화공원",3);
        station4TravelTime.put("동대문역사문화공원-동대문",1);
        station4TravelTime.put("동대문-혜화",3);
        station4TravelTime.put("혜화-한성대입구",2);
        station4TravelTime.put("한성대입구-성신여대입구",2);
        station4TravelTime.put("성신여대입구-길음",2);
        station4TravelTime.put("길음-미아사거리",3);
        station4TravelTime.put("미아사거리-미아",2);
        station4TravelTime.put("미아-수유",2);
        station4TravelTime.put("수유-쌍문",3);
        station4TravelTime.put("쌍문-창동",2);
        station4TravelTime.put("창동-노원",2);
        station4TravelTime.put("노원-상계",2);
        station4TravelTime.put("상계-불암산",3);
        //불암산에서 진접 까진 다른 경로
        station4TravelTime.put("불암산-별내별가람",5);
        station4TravelTime.put("별내별가람-오남",7);
        station4TravelTime.put("오남-진접",3);
        stationTravelTime.put(1004, station4TravelTime);

        // 8호선
        station8TravelTime.put("모란-수진",1);
        station8TravelTime.put("수란-신흥",2);
        station8TravelTime.put("신흥-단대오거리",2);
        station8TravelTime.put("단대오거리-남한산성입구(성남법원,검찰청)",1);
        station8TravelTime.put("남한산성입구(성남법원,검찰청)-산성",3);
        station8TravelTime.put("산성-남위례",2);
        station8TravelTime.put("남위례-복정",2);
        station8TravelTime.put("복정-장지",2);
        station8TravelTime.put("장지-문정",2);
        station8TravelTime.put("문정-가락시장",2);
        station8TravelTime.put("가락시장-송파",1);
        station8TravelTime.put("송파-석촌",2);
        station8TravelTime.put("석촌-잠실",3);
        station8TravelTime.put("잠실-몽촌토성(평화의문)",1);
        station8TravelTime.put("몽촌토성(평화의문)-강동구청",3);
        station8TravelTime.put("강동구청-천호(풍납토성)",2);
        station8TravelTime.put("천호(풍납토성)-암사",2);
        station8TravelTime.put("암사-암사역사공원",2);
        station8TravelTime.put("암사역사공원-장자호수공원",4);
        station8TravelTime.put("장자호수공원-구리",3);
        station8TravelTime.put("구리-동구릉",2);
        station8TravelTime.put("동구릉-다산",3);
        station8TravelTime.put("다산-별내",3);
        stationTravelTime.put(1008, station8TravelTime);

        //  공항철도
        stationARTravelTime.put("서울-공덕", 4);
        stationARTravelTime.put("공덕-홍대입구", 3);
        stationARTravelTime.put("홍대입구-디지털미디어시티", 4);
        stationARTravelTime.put("디지털미디어시티-마곡나루", 8);
        stationARTravelTime.put("마곡나루-김포공항", 3);
        stationARTravelTime.put("김포공항-계양", 6);
        stationARTravelTime.put("계양-검암", 6);
        stationARTravelTime.put("검암-청라국제도시", 5);
        stationARTravelTime.put("청라국제도시-영종", 9);
        stationARTravelTime.put("영종-운서", 4);
        stationARTravelTime.put("운서-공항화물청사", 4);
        stationARTravelTime.put("공항화물청사-인천공항1터미널", 5);
        stationARTravelTime.put("인천공항1터미널-인천공항2터미널", 8);
        stationTravelTime.put(1065, stationARTravelTime);

        // 경의중앙선 (Gyeongui Central Line)GCL
        // 메인노선(지평~가좌)
        stationGCLTravelTime.put("지평-용문",5);
        stationGCLTravelTime.put("용문-원덕",5);
        stationGCLTravelTime.put("원덕-양평",6);
        stationGCLTravelTime.put("양평-오빈",3);
        stationGCLTravelTime.put("오빈-아신",3);
        stationGCLTravelTime.put("아신-국수",4);
        stationGCLTravelTime.put("국수-신원",4);
        stationGCLTravelTime.put("신원-양수",4);
        stationGCLTravelTime.put("양수-운길산",3);
        stationGCLTravelTime.put("운길산-팔당",5);
        stationGCLTravelTime.put("팔당-도심",5);
        stationGCLTravelTime.put("도심-덕소",2);
        stationGCLTravelTime.put("덕소-양정",4);
        stationGCLTravelTime.put("양정-도뇽",4);
        stationGCLTravelTime.put("도뇽-구리",3);
        stationGCLTravelTime.put("구리-양원",4);
        stationGCLTravelTime.put("양원-망우",3);
        stationGCLTravelTime.put("망우-상봉",2);
        stationGCLTravelTime.put("상봉-중랑",2);
        stationGCLTravelTime.put("중랑-회기",3);
        stationGCLTravelTime.put("회기-청량리",4);
        stationGCLTravelTime.put("청량리-왕십리",4);
        stationGCLTravelTime.put("왕십리-응봉",2);
        stationGCLTravelTime.put("응봉-옥수",3);
        stationGCLTravelTime.put("옥수-한남",2);
        stationGCLTravelTime.put("한남-서빙고",3);
        stationGCLTravelTime.put("서빙고-이촌",3);
        stationGCLTravelTime.put("이촌-용산",4);
        stationGCLTravelTime.put("용산-효창공원앞",3);
        stationGCLTravelTime.put("효창공원앞-공덕",3);
        stationGCLTravelTime.put("공덕-서강대",3);
        stationGCLTravelTime.put("서강대-홍대입구",2);
        stationGCLTravelTime.put("홍대입구-가좌",2);
        stationGCLTravelTime.put("가좌-디지털미디어시티",3);
        //분기 1 (가좌~임진강)

        //경춘선(Gyeongchun Line) GL (춘천~상봉)
        stationGLTravelTime.put("춘천-남춘천",3);
        stationGLTravelTime.put("남춘천-김유정",6);
        stationGLTravelTime.put("김유정-강촌",6);
        stationGLTravelTime.put("강촌-백양리",5);
        stationGLTravelTime.put("백양리-굴봉산",3);
        stationGLTravelTime.put("굴봉산-가평",5);
        stationGLTravelTime.put("가평-상천",6);
        stationGLTravelTime.put("상천-청평",4);
        stationGLTravelTime.put("청평-대성리",7);
        stationGLTravelTime.put("대성리-마석",6);
        stationGLTravelTime.put("마석-천마산",2);
        stationGLTravelTime.put("천마산-평내호평",4);
        stationGLTravelTime.put("평내호평-금곡",4);
        stationGLTravelTime.put("금곡-사릉",4);
        stationGLTravelTime.put("사릉-퇴계원",3);
        stationGLTravelTime.put("퇴계원-별내",3);
        stationGLTravelTime.put("별내-갈매",2);
        stationGLTravelTime.put("갈매-신내",3);
        stationGLTravelTime.put("신내-망우",5);
        stationGLTravelTime.put("망우-상봉",2);
        //분기1 (상봉~광운대)
        stationGLTravelTime.put("상봉-광운대",8);
        //분기2 (상봉-청량리)
        stationGLTravelTime.put("상봉-중랑",3);
        stationGLTravelTime.put("중랑-회기",3);
        stationGLTravelTime.put("회기-청량리",3);
        stationTravelTime.put(1067, stationGLTravelTime);

        //수인분당선 (Suin Bundang Line)SBL
        // 메인노선
        stationSBLTravelTime.put("인천-신포",2);
        stationSBLTravelTime.put("신포-숭의",2);
        stationSBLTravelTime.put("숭의-인하대",3);
        stationSBLTravelTime.put("인하대-송도",2);
        stationSBLTravelTime.put("송도-연수",4);
        stationSBLTravelTime.put("연수-원인재",2);
        stationSBLTravelTime.put("원인재-남동인더스파크",2);
        stationSBLTravelTime.put("남동인더스파크-호구포",2);
        stationSBLTravelTime.put("호구포-인천논현",2);
        stationSBLTravelTime.put("인천논현-소래포구",2);
        stationSBLTravelTime.put("소래포구-월곶",2);
        stationSBLTravelTime.put("월곶-달월",2);
        stationSBLTravelTime.put("달월-오이도",3);
        stationSBLTravelTime.put("오이도-정왕",3);
        stationSBLTravelTime.put("정왕-신길온천",3);
        stationSBLTravelTime.put("신길온천-안산",3);
        stationSBLTravelTime.put("안산-초지",3);
        stationSBLTravelTime.put("초지-고잔",2);
        stationSBLTravelTime.put("고잔-중앙",2);
        stationSBLTravelTime.put("중앙-한대앞",3);
        stationSBLTravelTime.put("한대앞-사리",4);
        stationSBLTravelTime.put("사리-야목",5);
        stationSBLTravelTime.put("야목-어천",3);
        stationSBLTravelTime.put("어천-오목천",4);
        stationSBLTravelTime.put("오목천-고색",3);
        stationSBLTravelTime.put("고색-수원",4);
        stationSBLTravelTime.put("수원-매교",2);
        stationSBLTravelTime.put("매교-수원시청",3);
        stationSBLTravelTime.put("수원시청-매탄권선",3);
        stationSBLTravelTime.put("매탄권선-망포",2);
        stationSBLTravelTime.put("망포-영통",3);
        stationSBLTravelTime.put("영통-청명",2);
        stationSBLTravelTime.put("청명-상갈",3);
        stationSBLTravelTime.put("상갈-기흥",3);
        stationSBLTravelTime.put("기흥-신갈",2);
        stationSBLTravelTime.put("신갈-구성",3);
        stationSBLTravelTime.put("구성-보정",2);
        stationSBLTravelTime.put("보정-죽전",3);
        stationSBLTravelTime.put("죽전-오리",3);
        stationSBLTravelTime.put("오리-미금",2);
        stationSBLTravelTime.put("미금-정자",3);
        stationSBLTravelTime.put("정자-수내",2);
        stationSBLTravelTime.put("수내-서현",2);
        stationSBLTravelTime.put("서현-이매",2);
        stationSBLTravelTime.put("이매-야탑",3);
        stationSBLTravelTime.put("야탑-모란",3);
        stationSBLTravelTime.put("모란-태평",2);
        stationSBLTravelTime.put("태평-가천대",2);
        stationSBLTravelTime.put("가천대-복정",3);
        stationSBLTravelTime.put("복정-수서",4);
        stationSBLTravelTime.put("수서-대모산입구",4);
        stationSBLTravelTime.put("대모산입구-개포동",1);
        stationSBLTravelTime.put("개포동-구룡",2);
        stationSBLTravelTime.put("구룡-도곡",2);
        stationSBLTravelTime.put("도곡-한티",1);
        stationSBLTravelTime.put("한티-선릉",2);
        stationSBLTravelTime.put("선릉-선정릉",2);
        stationSBLTravelTime.put("선정릉-강남구청",2);
        stationSBLTravelTime.put("강남구청-압구정로데오",2);
        stationSBLTravelTime.put("압구정로데오-서울숲",2);
        stationSBLTravelTime.put("서울숲-왕십리",3);
        stationSBLTravelTime.put("왕십리-청량리",7);
        stationTravelTime.put(1075, stationSBLTravelTime);

        //신분당선(Shinbundang Line)SL
        //메인노선
        stationSLTravelTime.put("광교-광교중앙",3);
        stationSLTravelTime.put("광교중앙-상현",3);
        stationSLTravelTime.put("상현-성복",3);
        stationSLTravelTime.put("성복-수지구청",3);
        stationSLTravelTime.put("수지구청-동천",3);
        stationSLTravelTime.put("동천-미금",2);
        stationSLTravelTime.put("미금-정자",3);
        stationSLTravelTime.put("정자-판교",3);
        stationSLTravelTime.put("판교-청계산입구",6);
        stationSLTravelTime.put("청계산입구-양재시민의숲",3);
        stationSLTravelTime.put("양재시민의숲-양재",3);
        stationSLTravelTime.put("양재-강남",2);
        stationSLTravelTime.put("강남-신논현",2);
        stationSLTravelTime.put("신논현-논현",1);
        stationSLTravelTime.put("논현-신사",1);
        stationTravelTime.put(1077, stationSLTravelTime);

        //경강선(Gyeonggang Line)GGL (GL과 곂쳐서 GGL로 표기)
        //메인노선
        stationGGLTravelTime.put("여주-세종대왕릉",5);
        stationGGLTravelTime.put("세종대왕릉-부발",6);
        stationGGLTravelTime.put("부발-이천",4);
        stationGGLTravelTime.put("이천-신둔도예촌",6);
        stationGGLTravelTime.put("신둔도예촌-곤지암",6);
        stationGGLTravelTime.put("곤지암-초월",4);
        stationGGLTravelTime.put("초월-경기광주",5);
        stationGGLTravelTime.put("경기광주-삼동",5);
        stationGGLTravelTime.put("삼동-이매",5);
        stationGGLTravelTime.put("이매-성남",2);
        stationGGLTravelTime.put("판교-성남",2);
        stationTravelTime.put(1081, stationGGLTravelTime);

        //서해선(Seohae Line)SHL (SL과 곂쳐서 SHL로 표기)
        //메인노선
        stationSHLTravelTime.put("원시-시우",3);
        stationSHLTravelTime.put("시우-초지",3);
        stationSHLTravelTime.put("초지-선부",3);
        stationSHLTravelTime.put("선부-달미",3);
        stationSHLTravelTime.put("달미-시흥능곡",3);
        stationSHLTravelTime.put("시흥능곡-시흥시청",3);
        stationSHLTravelTime.put("시흥시청-신현",4);
        stationSHLTravelTime.put("신현-신천",5);
        stationSHLTravelTime.put("신천-시흥대야",2);
        stationSHLTravelTime.put("시흥대야-소새울",3);
        stationSHLTravelTime.put("소새울-소사",4);
        stationSHLTravelTime.put("소사-부천종합운동장",3);
        stationSHLTravelTime.put("부천종합운동장-원종",3);
        stationSHLTravelTime.put("원종-김포공항",5);
        stationSHLTravelTime.put("김포공항-능곡",8);
        stationSHLTravelTime.put("능곡-대곡",3);
        stationSHLTravelTime.put("대곡-곡산",3);
        stationSHLTravelTime.put("곡산-백마",3);
        stationSHLTravelTime.put("백마-풍산",2);
        stationSHLTravelTime.put("풍산-일산",2);
        stationTravelTime.put(1093, stationSHLTravelTime);

        //신림선(Sillim Line) SLL (SL과 곂쳐서 SSL로 표기)
        //메인노선
        stationSLLTravelTime.put("관악산-서울대벤처타운",2);
        stationSLLTravelTime.put("서울대벤처타운-서원",2);
        stationSLLTravelTime.put("서원-신림",2);
        stationSLLTravelTime.put("신림-당곡",1);
        stationSLLTravelTime.put("당곡-보라매병원",2);
        stationSLLTravelTime.put("보라매병원-보라매공원",1);
        stationSLLTravelTime.put("보라매공원-보라매",2);
        stationSLLTravelTime.put("보라매-서울지방병무청",2);
        stationSLLTravelTime.put("서울지방병무청-대방",2);
        stationSLLTravelTime.put("대방-샛강",1);
        stationTravelTime.put(1094, stationSLLTravelTime);

        //GTX-A(GTX) 메인 노선
        stationGTXTravelTime.put("동탄-구성",7);
        stationGTXTravelTime.put("구성-성남",7);
        stationGTXTravelTime.put("성남-수서",7);

        //GTX-A 두번째 메인 노선?
        stationGTXTravelTime.put("서울역-연신내",6);
        stationGTXTravelTime.put("연신내-대곡",6);
        stationGTXTravelTime.put("대곡-킨텍스",5);
        stationGTXTravelTime.put("킨텍스-운정중앙",4);
        stationTravelTime.put(1032, stationGTXTravelTime);

    }

    // 시간 조회
    public static Map<String, Integer> getStationTravelTime(int subwayCode) {
        return stationTravelTime.getOrDefault(subwayCode, new HashMap<>());
    }

    // 두 경로가 같은 방향인지 (즉, 시작부터 최소한의 공통 구간을 가지는지) 확인
    protected static boolean isSameDirection(List<String> r1, List<String> r2) {
        int minSize = Math.min(r1.size(), r2.size());
        for (int i = 0; i < minSize; i++) {
            if (!r1.get(i).equals(r2.get(i))) return false;
        }
        return true;
    }

    // KMP 알고리즘을 이용하여, sub 리스트가 full 리스트 내에 연속적으로 존재하는지 확인 (O(m+n))
    protected static boolean isSubPath(List<String> sub, List<String> full) {
        if (sub.size() > full.size())
            return false;

        String[] pattern = sub.toArray(new String[0]);
        String[] text = full.toArray(new String[0]);
        int[] lps = computeLPSArray(pattern);

        int i = 0, j = 0;  // i: pattern index, j: text index
        while (j < text.length) {
            if (pattern[i].equals(text[j])) {
                i++;
                j++;
                if (i == pattern.length)
                    return true;
            } else {
                if (i != 0)
                    i = lps[i - 1];
                else
                    j++;
            }
        }
        return false;
    }

    // KMP 알고리즘용 LPS (Longest Prefix Suffix) 배열 계산
    private static int[] computeLPSArray(String[] pattern) {
        int[] lps = new int[pattern.length];
        int len = 0;  // 이전 최대 접두사-접미사 길이
        lps[0] = 0;
        int i = 1;
        while (i < pattern.length) {
            if (pattern[i].equals(pattern[len])) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        return lps;
    }

    // BFS를 사용해 출발역부터 도착역까지 최단 경로 찾기
    protected static List<String> findRoute(Map<String, List<String>> network, String start, String end) {
        Queue<List<String>> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        // 출발역 초기화
        List<String> startPath = new ArrayList<>();
        startPath.add(start);
        queue.offer(startPath);
        visited.add(start);

        while (!queue.isEmpty()) {
            List<String> path = queue.poll();
            String lastStation = path.get(path.size() - 1);

            // 도착역에 도달 시 경로 반환
            if (lastStation.equals(end)) {
                return path;
            }

            // 이웃 역 탐색
            for (String neighbor : network.getOrDefault(lastStation, new LinkedList<>())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    List<String> newPath = new ArrayList<>(path);
                    newPath.add(neighbor);
                    queue.offer(newPath);
                }
            }
        }

        // 경로를 찾을 수 없는 경우 빈 리스트 반환
        return new ArrayList<>();
    }
}
