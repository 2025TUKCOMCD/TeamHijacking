package com.example.back.service;

import org.springframework.stereotype.Service;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class SubwayService {
    private Map<String, LinkedList<String>> network;
    private static Map<String, Integer> station1TravelTime;
    private static Map<String, Integer> station4TravelTime;
    private static final Map<Integer, Integer> subwayCodeMap = Map.ofEntries(
            Map.entry(1, 1001), Map.entry(2, 1002), Map.entry(3, 1003), Map.entry(4, 1004),
            Map.entry(5, 1005), Map.entry(6, 1006), Map.entry(7, 1007), Map.entry(8, 1008),
            Map.entry(9, 1009), Map.entry(91, 1032), Map.entry(104, 1063), Map.entry(101, 1065),
            Map.entry(108, 1067), Map.entry(116, 1075), Map.entry(109, 1077), Map.entry(112, 1081),
            Map.entry(113, 1092), Map.entry(114, 1093), Map.entry(117, 1094)
    );
    // 지하철 1호선의 구간별 걸리는 시간
    private void initialize1TravelTime() {
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
    }
    //지하철 4호선의 걸리는 시간
    private void initialize4TravelTime() {
        //  메인 노선 (오이도 ~ 불암산)
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
        station4TravelTime.put("사당-총신대입구",2); // 총신대입구랑 이수역 이름이 두개인데 뭐로 해야함?
        station4TravelTime.put("총신대입구-동작",3);
        station4TravelTime.put("동작-이촌",3);
        station4TravelTime.put("이촌-신용산",3);
        station4TravelTime.put("신용산-삼각지",1);
        station4TravelTime.put("삼각지-숙대입구",2);
        station4TravelTime.put("숙대입구-서울역",2);
        station4TravelTime.put("서울역-회현",2);
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



    }
    // 지하철 8호선의 구간별 걸리는 시간
    private void initialize8TravelTime() {
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


    }
    // 지하철 공항철도의 구간별 걸리는 시간 Airport Railroad
    private void initializeARTravelTime() {
        stationARTravelTime.put("인천공항2터미널-인천공항1터미널",8);
        stationARTravelTime.put("인천공항1터미널-공항화물청사",5);
        stationARTravelTime.put("공항화물청사-운서",4);
        stationARTravelTime.put("운서-영종");
        stationARTravelTime.put("영종-청라국제도시",9);
        stationARTravelTime.put("청라국제도시-검암",5);
        stationARTravelTime.put("검암-계양",6);
        stationARTravelTime.put("계양-김포공항",6);
        stationARTravelTime.put("김포공항-마곡나루",3);
        stationARTravelTime.put("마곡나루-디지털미디어시티",8);
        stationARTravelTime.put("디지털미디어시티-홍대입구",4);
        stationARTravelTime.put("홍대입구-공덕",3);
        stationARTravelTime.put("공덕-서울",4);
    }
    // 지하철 코드 - 도시 매핑 메서드
    public Map<Integer, Integer> getSubwayCodeToCityMapping() {
        Map<Integer, Integer> subwayCodeToCity = new HashMap<>();
        // 서울 지하철
        for (int code : new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 91, 101, 102, 104, 107, 108, 109, 110, 112, 113, 114, 115, 116, 117, 21, 22}) {
            subwayCodeToCity.put(code, 10);
        }
        // 부산
        for (int code : new int[]{71, 72, 73, 74, 78, 79}) {
            subwayCodeToCity.put(code, 20);
        }
        // 대구
        for (int code : new int[]{41, 42, 43, 48}) {
            subwayCodeToCity.put(code, 30);
        }
        // 광주
        subwayCodeToCity.put(51, 40);
        // 대전
        subwayCodeToCity.put(31, 50);

        return subwayCodeToCity;
    }
    // 오디세이 -> 서울 지하철 노선 코드 변환
    public int convertSubwayCode(int subwayCode) {
        return subwayCodeMap.getOrDefault(subwayCode, 0);
    }

    // 서울 지하철 특수 코드 확인
    public boolean isSpecialSeoulCode(int subwayCode) {
        List<Integer> specialSeoulCodes = Arrays.asList(110, 115, 21, 22, 107);
        return specialSeoulCodes.contains(subwayCode);
    }

    // 서울 지하철 현재 시간 가져오기
    public static Time getSeoulCurrentTime() {
        ZoneId seoulZoneId = ZoneId.of("Asia/Seoul");
        LocalTime seoulTime = ZonedDateTime.now(seoulZoneId).toLocalTime();
        return Time.valueOf(seoulTime);
    }

    // 서울 지하철 도착 정보 조회
    public Map<String, Object> getTimeAndDayType(int subwayCode) {
        Time seoulTime = getSeoulCurrentTime();
        String currentDayType = getCurrentDayType(subwayCode);
        System.out.println("현재 요일 타입: " + currentDayType);
        Map<String, Object> result = new HashMap<>();
        result.put("seoulTime", seoulTime);
        result.put("currentDayType", currentDayType);
        return result;
    }

    // 현재 요일 계산
    public String getCurrentDayType(int subwayCode) {
        ZoneId seoulZoneId = ZoneId.of("Asia/Seoul");
        DayOfWeek currentDay = ZonedDateTime.now(seoulZoneId).getDayOfWeek();
        if (currentDay == DayOfWeek.SATURDAY || currentDay == DayOfWeek.SUNDAY) {
            if (isWeekendRoute(subwayCode)) {
                if (currentDay == DayOfWeek.SATURDAY) {
                    return "sat"; // 토요일
                } else {
                    return "sun"; // 일요일
                }
            } else {
                return "wknd"; // 주말 (공통)
            }
        } else {
            return "wkdy"; // 평일
        }
    }

    // 특정 route_id가 주말에 토요일과 일요일을 구분해야 하는지 확인
    public boolean isWeekendRoute(int subwayCode) {
        List<Integer> weekendSpecificRoutes = Arrays.asList(71, 72, 73, 74, 41, 42, 43, 51, 110);
        return weekendSpecificRoutes.contains(subwayCode);
    }


    // 생성자: 네트워크를 구축합니다.
    private SubwayService() {
        network = new HashMap<>();
        station1TravelTime = new HashMap<>();
        //station2TravelTime = new HashMap<>();
        build1Network();
        initialize1TravelTime();
    }

    // 네트워크 구축: 메인 노선 및 분기 노선들을 추가합니다.
    private void build1Network() {
        // 메인 노선 (연천 ~ 구로)
        String[] mainBranch = {
                "연천", "전곡", "청산", "소요산", "동두천", "보산", "동두천중앙",
                "지행", "덕정", "덕계", "양주", "녹양", "가능", "의정부", "회룡", "망월사",
                "도봉산", "도봉", "방학", "창동", "녹천", "월계", "광운대", "석계",
                "신이문", "외대앞", "회기", "청량리", "제기동", "신설동", "동묘앞",
                "동대문", "종로5가", "종로3가", "종각", "시청", "서울", "남영",
                "용산", "노량진", "대방", "신길", "영등포", "신도림", "구로"
        };
        addRoute(network, mainBranch);

        // 분기 1: 구로에서 분기하여 구일 ~ 인천 (예시)
        String[] branch1 = {
                "구일", "개봉", "오류동", "온수", "역곡", "소사", "부천", "중동",
                "송내", "부개", "부평", "백운", "동암", "간석", "주안", "도화",
                "제물포", "도원", "동인천", "인천"
        };
        addRoute(network, branch1);
        addLinkedConnection(network, "구로", "구일");

        // 분기 2: 구로에서 분기하여 가산디지털단지, 독산, 금천구청
        String[] branch2 = {"가산디지털단지", "독산", "금천구청"};
        addRoute(network, branch2);
        addLinkedConnection(network, "구로", "가산디지털단지");

        // 분기2-1: 금천구청에서 광명
        addLinkedConnection(network, "금천구청", "광명");

        // 분기2-2: 가산디지털단지부터 병점까지
        String[] branch2_2 = {
                "가산디지털단지", "독산", "금천구청", "석수", "관악", "안양", "명학",
                "금정", "군포", "당정", "의왕", "성균관대", "화서", "수원", "세류", "병점"
        };
        addRoute(network, branch2_2);

        // 분기2-2-1: 병점에서 서동탄
        addLinkedConnection(network, "병점", "서동탄");

        // 분기2-2-2: 병점부터 신창까지
        String[] branch2_2_2 = {
                "병점", "세마", "오산대", "오산", "진위", "송탄", "서정리", "지제",
                "평택", "성환", "직산", "두정", "천안", "봉명", "쌍용(나사렛대)",
                "아산", "탕정", "배방", "온양온천", "신창"
        };
        addRoute(network, branch2_2_2);
    }.
    private void build8Network() {
        String[] mainBranch = {
                "모란", "수진", "신흥", "단대오거리", "남한산성입구(성남법원,검찰청)", "산성", "남위례",
                "복정", "장지", "문정", "가락시장", "송파", "석촌", "잠실", "몽촌토성(평화의문)",
                "강동구청", "천호(풍납토성)", "암사", "암사역사공원", "장자호수공원", "구리", "동구릉",
                "다산", "별내"
        };
        //addRoute(network, mainBranch); <- 질문1 이거 이름 다르게 안하고 추가해도 되는겨?

    }
    private void buildARNetwork() {
        String[] mainBranch = {
                "인천공항2터미널", "인천공항1터미널", "공항화물청사", "운서", "영종", "청라국제도시",
                "검암", "계양", "김포공항", "마곡나루", "디지털미디어시티", "홍대입구", "공덕", "서울역"
        }
        //addRoute(network, mainBranch;;<- 위와같음
    }
    // 경로 탐색
    public List<String> findRoute(String start, String end) {
        return findRoute(network, start, end);
    }

    // 경로 비교 메서드
    public int compareRoutes(String aStart, String aEnd, String bStart, String bEnd) {
        System.out.println(aStart);
        System.out.println(aEnd);
        System.out.println(bStart);
        System.out.println(bEnd);

        List<String> routeA = findRoute(aStart, aEnd);
        List<String> routeB = findRoute(bStart, bEnd);

        System.out.println(routeA);
        System.out.println(routeB);

        if (routeA.isEmpty() || routeB.isEmpty()) {
            System.out.println("입력한 경로 중 하나 이상에서 경로를 찾을 수 없습니다.");
            return -1;
        }
        // 출발역이 같다고 가정하므로, 두 경로의 진행 방향 (초기 구간)이 같은지 확인
        else if (!isSameDirection(routeA, routeB)) {
            System.out.println("두 경로는 시작점은 같으나, 진행 방향이 다릅니다.");
            return 0;
        }
        // A 경로가 B의 부분 경로인 경우 (A의 길이가 B보다 짧으면서 B의 접두어인지 KMP로 확인)
        else if (routeA.size() < routeB.size() && isSubPath(routeA, routeB)) {
            System.out.println("A 경로는 B 경로의 연속적인 부분 경로입니다. (A ⊂ B)");
            return 1;
        }
        else {
            System.out.println("A 경로는 B 경로와 같은 방향이지만, 포함 관계에 있지 않습니다.");
            return 2;
        }
    }

    // 지정된 역 배열을 네트워크에 순서대로 추가 (양방향 연결)
    private static void addRoute(Map<String, LinkedList<String>> network, String[] stations) {
        for (int i = 0; i < stations.length; i++) {
            network.putIfAbsent(stations[i], new LinkedList<>());
            if (i > 0) {
                addLinkedConnection(network, stations[i - 1], stations[i]);
            }
        }
    }

    // 두 역 사이에 양방향 연결을 추가합니다.
    private static void addLinkedConnection(Map<String, LinkedList<String>> network, String station1, String station2) {
        network.putIfAbsent(station1, new LinkedList<>());
        network.putIfAbsent(station2, new LinkedList<>());
        if (!network.get(station1).contains(station2))
            network.get(station1).add(station2);
        if (!network.get(station2).contains(station1))
            network.get(station2).add(station1);
    }

    // BFS를 사용해 출발역부터 도착역까지 최단 경로 찾기
    private static List<String> findRoute(Map<String, LinkedList<String>> network, String start, String end) {
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

    static int calculateTravelTime(List<String> route, String direction,int subwayCode) {
        int totalTime = 0;
        // direction에 따른 route.get(i)와 route.get(i+1) 간의 이동 시간 계산
        // 상행
        if(direction.equals("상행")){
            for (int i = 0; i < route.size() - 1; i++) {
                String key = route.get(i) + "-" + route.get(i + 1);
                System.out.println("Key: " + key);
                // stationId에 따른 네트워크 선택 및 시간 계산
                if (subwayCode == 1) {
                    totalTime += station1TravelTime.getOrDefault(key, 0);
                }
                System.out.println("Total Time: " + totalTime);
            }
        }
        // 하행
        else if(direction.equals("하행")){
            for (int i = route.size() - 1; i > 0; i--) {
                String key = route.get(i) + "-" + route.get(i - 1);
                System.out.println("Key: " + key);
                if(subwayCode == 1) {
                    totalTime += station1TravelTime.getOrDefault(key, 0);
                }
                System.out.println("Total Time: " + totalTime);
            }
        }
        // 두 역 간 이동 시간을 더하기
        return totalTime;
    }

    // 두 경로가 같은 방향인지 (즉, 시작부터 최소한의 공통 구간을 가지는지) 확인
    private static boolean isSameDirection(List<String> r1, List<String> r2) {
        int minSize = Math.min(r1.size(), r2.size());
        for (int i = 0; i < minSize; i++) {
            if (!r1.get(i).equals(r2.get(i))) return false;
        }
        return true;
    }

    // KMP 알고리즘을 이용하여, sub 리스트가 full 리스트 내에 연속적으로 존재하는지 확인 (O(m+n))
    private static boolean isSubPath(List<String> sub, List<String> full) {
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
}


