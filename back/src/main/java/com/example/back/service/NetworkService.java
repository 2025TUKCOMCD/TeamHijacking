package com.example.back.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NetworkService {

    // 네트워크 선언
    private Map<Integer, Map<String, List<String>>> networks;

    public NetworkService() {
        networks = new HashMap<>();
    }

    // 네트워크 생성
    @PostConstruct
    private void buildNetwork() {
        int[] subwayLines = {1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009,
                1032, 1063, 1065, 1067, 1075, 1077, 1081, 1092, 1093, 1094};

        for (int line : subwayLines) {
            networks.put(line, new HashMap<>());
        }

        // 1호선 메인 노선 설정
        addRoute(1001, new String[]{"연천", "전곡", "청산", "소요산", "동두천", "보산", "동두천중앙",
                "지행", "덕정", "덕계", "양주", "녹양", "가능", "의정부", "회룡", "망월사",
                "도봉산", "도봉", "방학", "창동", "녹천", "월계", "광운대", "석계",
                "신이문", "외대앞", "회기", "청량리", "제기동", "신설동", "동묘앞",
                "동대문", "종로5가", "종로3가", "종각", "시청", "서울", "남영",
                "용산", "노량진", "대방", "신길", "영등포", "신도림", "구로"});

        // 분기1: 구로 → 구일 → 인천
        addRoute(1001, new String[]{"구일", "개봉", "오류동", "온수", "역곡", "소사", "부천", "중동",
                "송내", "부개", "부평", "백운", "동암", "간석", "주안", "도화",
                "제물포", "도원", "동인천", "인천"});
        addLinkedConnection(1001, "구로", "구일");

        // 분기2: 구로 → 가산디지털단지 → 금천구청
        addRoute(1001, new String[]{"가산디지털단지", "독산", "금천구청"});
        addLinkedConnection(1001, "구로", "가산디지털단지");
        addLinkedConnection(1001, "금천구청", "광명");

        // 분기2-2: 가산디지털단지 → 병점
        addRoute(1001, new String[]{"가산디지털단지", "독산", "금천구청", "석수", "관악", "안양", "명학",
                "금정", "군포", "당정", "의왕", "성균관대", "화서", "수원", "세류", "병점"});
        addLinkedConnection(1001, "병점", "서동탄");

        // 분기2-2-2: 병점 → 신창
        addRoute(1001, new String[]{"병점", "세마", "오산대", "오산", "진위", "송탄", "서정리", "지제",
                "평택", "성환", "직산", "두정", "천안", "봉명", "쌍용(나사렛대)",
                "아산", "탕정", "배방", "온양온천", "신창"});

        //4호선 메인 노선 설정
        addRoute(1004,new String[]{"오이도", "정왕", "신길온천", "안산", "초지", "고잔", "중앙", "한대앞", "상록수",
                "반월", "대야미", "수리산", "산본", "금정", "범계", "평촌", "인덕원",
                "정부과천청사", "과천", "대공원", "경마공원", "선바위", "남태령", "사당",
                "총신대입구(이수)", "동작", "이촌", "신용산", "삼각지", "숙대입구", "서울",
                "회현", "명동", "충무로", "동대문역사문화공원", "동대문", "혜화",
                "한성대입구", "성신여대입구", "길음", "미아사거리", "미아", "수유",
                "쌍문", "창동", "노원", "상계", "불암산"});

        // 8호선 메인 노선 설정
        addRoute(1008,new String[]{"모란", "수진", "신흥", "단대오거리", "남한산성입구(성남법원,검찰청)", "산성", "남위례",
                "복정", "장지", "문정", "가락시장", "송파", "석촌", "잠실", "몽촌토성(평화의문)",
                "강동구청", "천호(풍납토성)", "암사", "암사역사공원", "장자호수공원", "구리", "동구릉",
                "다산", "별내"});

        // 공항철도 메인 노선 설정
        addRoute(1065, new String[]{"인천공항2터미널", "인천공항1터미널", "공항화물청사", "운서", "영종", "청라국제도시",
                "검암", "계양", "김포공항", "마곡나루", "디지털미디어시티", "홍대입구", "공덕", "서울"});
    }

    // 다른 클래스에서 networks 가져오는 메서드
    public Map<String, List<String>> getNetwork(int subwayCode) {
        return networks.getOrDefault(subwayCode, new HashMap<>());
    }

    // 노선에 역을 추가
    private void addRoute(int line, String[] stations) {
        Map<String, List<String>> route = networks.get(line);
        for (int i = 0; i < stations.length - 1; i++) {
            String current = stations[i];
            String next = stations[i + 1];

            route.computeIfAbsent(current, k -> new ArrayList<>()).add(next);
            route.computeIfAbsent(next, k -> new ArrayList<>()).add(current);
        }
    }

    // 두 역 사이에 양방향 연결을 추가
    private void addLinkedConnection(int line, String station1, String station2) {
        networks.putIfAbsent(line, new HashMap<>());

        networks.get(line).putIfAbsent(station1, new LinkedList<>());
        networks.get(line).putIfAbsent(station2, new LinkedList<>());

        if (!networks.get(line).get(station1).contains(station2)) {
            networks.get(line).get(station1).add(station2);
        }
        if (!networks.get(line).get(station2).contains(station1)) {
            networks.get(line).get(station2).add(station1);
        }
    }
}
