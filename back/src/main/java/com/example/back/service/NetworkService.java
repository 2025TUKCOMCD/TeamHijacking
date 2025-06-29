package com.example.back.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NetworkService {

    // 네트워크 선언
    private Map<String, Map<String, List<String>>> networks;

    public NetworkService() {
        networks = new HashMap<>();
    }

    // 네트워크 생성
    @PostConstruct
    private void buildNetwork() {
        String[] subwayLines = {"1001","1002", "1003", "1004", "1005",
                "1006", "1007", "1008", "1009", "1032-1","1032-2",
                "1063", "1065", "1067", "1075", "1077",
                "1081", "1092", "1093", "1094"};


        for (String line : subwayLines) {
            networks.put(line, new HashMap<>());
        }

        // 1호선 메인 노선 설정
        addRoute("1001", new String[]{"연천", "전곡", "청산", "소요산", "동두천", "보산", "동두천중앙",
                "지행", "덕정", "덕계", "양주", "녹양", "가능", "의정부", "회룡", "망월사",
                "도봉산", "도봉", "방학", "창동", "녹천", "월계", "광운대", "석계",
                "신이문", "외대앞", "회기", "청량리", "제기동", "신설동", "동묘앞",
                "동대문", "종로5가", "종로3가", "종각", "시청", "서울", "남영",
                "용산", "노량진", "대방", "신길", "영등포", "신도림", "구로"});

        // 분기1: 구로 → 구일 → 인천
        addRoute("1001", new String[]{"구일", "개봉", "오류동", "온수", "역곡", "소사", "부천", "중동",
                "송내", "부개", "부평", "백운", "동암", "간석", "주안", "도화",
                "제물포", "도원", "동인천", "인천"});
        addLinkedConnection("1001", "구로", "구일");

        // 분기2: 구로 → 가산디지털단지 → 금천구청
        addRoute("1001", new String[]{"가산디지털단지", "독산", "금천구청"});
        addLinkedConnection("1001", "구로", "가산디지털단지");
        addLinkedConnection("1001", "금천구청", "광명");

        // 분기2-2: 가산디지털단지 → 병점
        addRoute("1001", new String[]{"가산디지털단지", "독산", "금천구청", "석수", "관악", "안양", "명학",
                "금정", "군포", "당정", "의왕", "성균관대", "화서", "수원", "세류", "병점"});
        addLinkedConnection("1001", "병점", "서동탄");

        // 분기2-2-2: 병점 → 신창
        addRoute("1001", new String[]{"병점", "세마", "오산대", "오산", "진위", "송탄", "서정리", "지제",
                "평택", "성환", "직산", "두정", "천안", "봉명", "쌍용(나사렛대)",
                "아산", "탕정", "배방", "온양온천", "신창"});

        // 2호선 내선 순환 노선 설정
        addRoute("1002", new String[]{
                "시청", "을지로입구", "을지로3가", "을지로4가", "동대문역사문화공원", "신당",
                "상왕십리", "왕십리", "한양대", "뚝섬", "성수", "건대입구", "구의", "강변", "잠실나루",
                "잠실", "잠실새내", "종합운동장", "삼성", "선릉", "역삼", "강남", "교대", "서초",
                "방배", "사당", "낙성대", "서울대입구", "봉천", "신림", "신대방", "구로디지털단지",
                "대림", "신도림", "문래", "영등포구청", "당산", "합정", "홍대입구", "신촌",
                "이대", "아현", "충정로"
        });

        // 내선 순환 연결: 충정로 다음은 시청
        addLinkedConnection("1002", "충정로", "시청");
        // 지선 성수-신설동
        addRoute("1002", new String[]{"용답", "신답", "용두", "신설동"});
        addLinkedConnection("1002", "성수", "용답");

        // 지선 신도림-까치산
        addRoute("1002", new String[]{"도림천", "양천구청", "신정네거리", "까치산"});
        addLinkedConnection("1002", "신도림", "도림천");

        // 3호선 메인 노선 설정
        addRoute("1003", new String[]{
                "대화", "주엽", "정발산", "마두", "백석", "대곡", "화정", "원당",
                "원흥", "삼송", "지축", "구파발", "연신내", "불광", "녹번", "홍제", "무악재",
                "독립문", "경복궁(정부서울청사)", "안국", "종로3가", "을지로3가", "충무로",
                "동대입구", "약수", "금호", "옥수", "압구정", "신사", "잠원", "고속터미널",
                "교대", "남부터미널(예술의전당)", "양재", "매봉", "도곡", "대치", "학여울",
                "대청", "일원", "수서", "가락시장", "경찰병원", "오금"
        });

        //4호선 메인 노선 설정
        addRoute("1004",new String[]{"오이도", "정왕", "신길온천", "안산", "초지", "고잔", "중앙", "한대앞", "상록수",
                "반월", "대야미", "수리산", "산본", "금정", "범계", "평촌", "인덕원",
                "정부과천청사", "과천", "대공원", "경마공원", "선바위", "남태령", "사당",
                "총신대입구(이수)", "동작", "이촌", "신용산", "삼각지", "숙대입구", "서울",
                "회현", "명동", "충무로", "동대문역사문화공원", "동대문", "혜화",
                "한성대입구", "성신여대입구", "길음", "미아사거리", "미아", "수유",
                "쌍문", "창동", "노원", "상계", "불암산"});

        // 5호선 메인 노선 설정
        addRoute("1005", new String[]{
                "방화", "개화산", "김포공항", "송정", "마곡", "발산", "우장산", "화곡",
                "까치산", "신정", "목동", "오목교", "영등포구청", "영등포시장", "신길", "여의도",
                "여의나루", "마포", "공덕", "애오개", "충정로", "서대문", "광화문", "종로3가",
                "을지로4가", "동대문역사문화공원", "청구", "신금호", "행당", "왕십리", "마장",
                "답십리", "장한평", "군자", "아차산(어린이대공원후문)", "광나루(장신대)", "천호(풍납토성)", "강동"
        });
        // 5호선 지선 1: 강동 → 하남검단산 방면 (메인 노선에서 분기)
        addRoute("1005", new String[]{
                "길동", "굽은다리(강동구민회관)", "명일", "고덕", "상일동", "강일", "미사",
                "하남풍산", "하남시청", "하남검단산"
        });
        addLinkedConnection("1005", "강동", "길동");

        // 5호선 지선 2: 강동 → 마천 방면 (메인 노선에서 분기)
        addRoute("1005", new String[]{
                "둔촌동", "올림픽공원", "방이", "오금", "개롱", "거여", "마천"
        });
        addLinkedConnection("1005", "강동", "둔촌동");

        // 6호선 메인 노선 설정
        addRoute("1006", new String[]{
                "응암", "역촌", "불광", "독바위", "연신내", "구산", "새절(신사)", "증산",
                "디지털미디어시티", "월드컵경기장", "마포구청", "망원", "합정", "상수", "광흥창", "대흥(서강대앞)",
                "공덕", "효창공원앞", "삼각지", "녹사평(용산구청)", "이태원", "한강진", "버티고개", "약수",
                "청구", "신당", "동묘앞", "창신", "돌곶이", "상월곡(한국과학기술연구원)", "월곡(동덕여대)",
                "고려대", "안암(고대병원앞)", "보문", "신설동", "동묘앞", "창신", "봉화산" // 봉화산까지 순환 및 연장
        });

        // 7호선 메인 노선 설정
        addRoute("1007", new String[]{
                "장암", "도봉산", "수락산", "마들", "노원", "중계", "하계", "공릉(서울과학기술대)",
                "태릉입구", "먹골", "묵동", "상봉", "중화", "면목", "사가정", "용마산", "중곡",
                "군자", "어린이대공원", "건대입구", "뚝섬유원지", "청담", "강남구청", "학동", "논현",
                "반포", "고속터미널", "내방", "총신대입구(이수)", "남성", "숭실대입구(살피재)", "상도",
                "장승배기", "신대방삼거리", "보라매", "신풍", "대림", "남구로", "가산디지털단지",
                "철산", "광명사거리", "천왕", "온수", "까치울", "부천종합운동장", "춘의", "신중동",
                "부천시청", "상동", "삼산체육관", "굴포천", "부평구청", "산곡", "석남"
        });

        // 8호선 메인 노선 설정
        addRoute("1008",new String[]{"모란", "수진", "신흥", "단대오거리", "남한산성입구(성남법원,검찰청)", "산성", "남위례",
                "복정", "장지", "문정", "가락시장", "송파", "석촌", "잠실", "몽촌토성(평화의문)",
                "강동구청", "천호(풍납토성)", "암사", "암사역사공원", "장자호수공원", "구리", "동구릉",
                "다산", "별내"});

        addRoute("1009", new String[]{
                "개화", "김포공항", "공항시장", "신방화", "마곡나루", "양천향교", "가양", "증미",
                "등촌", "염창", "신목동", "선유도", "당산", "국회의사당", "여의도", "샛강",
                "노량진", "노들", "흑석", "동작", "구반포", "신반포", "고속터미널",
                "사평", "신논현", "언주", "선정릉", "삼성중앙", "봉은사", "종합운동장", "삼전",
                "석촌고분", "석촌", "송파나루", "한성백제", "올림픽공원", "둔촌오륜", "중앙보훈병원"
        });

        //경의중앙선 메인 노선 설정
        addRoute("1063", new String[]{"지평", "용문", "원덕", "양평", "오빈", "아신", "국수", "신원", "양수", "운길산",
                "팔당", "도심", "덕소", "양정", "도농", "구리", "양원", "망우", "상봉", "중랑", "회기", "청량리", "왕십리",
                "응봉", "옥수", "한남", "서빙고", "이촌", "용산", "효창공원앞", "공덕", "서강대", "홍대입구", "가좌"});

        //분기 가좌 -> 신촌 -> 서울역
        addRoute("1063", new String[]{"신촌(경의중앙선)", "서울역"});
        addLinkedConnection("1063", "가좌", "신촌(경의중앙선)");

        //분기 가좌 -> 디지털미디어시티 -> 임진강
        addRoute("1063", new String[]{"디지털미디어시티", "수색", "한국항공대", "강매", "행신", "능곡", "대곡", "곡산",
                "백마", "풍산", "일산", "탄현", "야당", "운정", "금릉", "금촌", "월롱", "파주", "문산", "운천", "임진강"});
        addLinkedConnection("1063", "가좌", "디지털미디어시티");

        // 공항철도 메인 노선 설정
        addRoute("1065", new String[]{"서울", "공덕", "홍대입구", "디지털미디어시티", "마곡나루", "김포공항", "계양", "검암",
                "청라국제도시", "영종", "운서", "공항화물청사", "인천공항1터미널", "인천공항2터미널"});

        //경춘선 메인 노선 춘천 -> 상봉
        addRoute("1067", new String[]{"춘천", "남춘천", "김유정", "강촌", "백양리", "굴봉산", "가평", "상천", "청평",
                "대성리", "마석", "천마산", "평내호평", "금곡", "사릉", "퇴계원", "별내", "갈매", "신내", "망우", "상봉"});
        //분기1 : 상봉 -> 광운대
        addRoute("1067", new String[]{"광운대"});
        addLinkedConnection("1067","상봉", "광운대");
        //분기2 : 상봉 -> 중랑 -> 청량리
        addRoute("1067", new String[]{"중랑", "회기", "청량리"});
        addLinkedConnection("1067","상봉", "중랑");

        //수인분당선 메인 노선 인천 -> 청량리
        addRoute("1075", new String[]{"인천", "신포", "숭의", "인하대", "송도", "연수", "원인재", "남동인더스파크", "호구포",
                "인천논현", "소래포구", "월곶", "달월", "오이도", "정왕", "신길온천", "안산", "초지", "고잔", "중앙", "한대앞",
                "사리", "야목", "어천", "오목천", "고색", "수원", "매교", "수원시청", "매탄권선", "망포", "영통", "청명", "상갈",
                "기흥", "신갈", "구성", "보정", "죽전", "오리", "미금", "정자", "수내", "서현", "이매", "야탑", "모란", "태평",
                "가천대", "복정", "수서", "대모산입구","개포동", "구룡", "도곡", "한티", "선릉", "선정릉", "강남구청", "압구정로데오", "서울숲",
                "왕십리", "청량리"});

        //신분당선 메인 노선 광교 -> 신사
        addRoute("1077", new String[]{"광교", "광교중앙", "상현", "성복", "수지구청", "동천", "미금", "정자", "판교",
                "청계산입구"," 양재시민의숲", "양재", "강남", "신논현", "논현", "신사"});

        //경강선 메인 노선 여주 -> 판교
        addRoute("1081", new String[]{"여주", "세종대왕릉", "부발", "이천", "신둔도예촌", "곤지암", "초월", "경기광주",
                "삼동", "이매", "성남", "판교"});

        // 우이신설선 메인 노선
        addRoute("1092", new String[]{
                "북한산우이", "솔밭공원", "4.19민주묘지", "가오리", "화계", "삼양", "삼양사거리",
                "솔샘", "북한산보국문", "정릉", "성신여대입구", "보문", "신설동"
        });
        //서해선 메인 노선 원시 -> 일산
        addRoute("1093", new String[]{"원시", "시우", "초지", "선부", "달미", "시흥능곡", "시흥시청", "신현", "신천",
                "시흥대야", "소새울", "소사", "부천종합운동장", "원종", "김포공항", "능곡", "대곡", "곡산", "백마", "풍산", "일산"});
        //신림선 메인 노선 관악산 -> 샛강

        addRoute("1094", new String[]{"관악산", "서울대벤처타운", "서원", "신림", "당곡", "보라매병원", "보라매공원",
                "보라매", "서울지방병무청", "대방", "샛강"});

        //GTX-A 메인 노선 동탄 -> 수서
        addRoute("1032-1", new String[]{"동탄", "구성", "성남", "수서"});

        //GTX-A 두번째 메인 노선..? 서울역 -> 운정중앙
        addRoute("1032-2", new String[]{"서울역", "연신내", "대곡", "킨텍스", "운정중앙"});

    }

    public Map<String, List<String>> getNetwork(int subwayCode, String startName) {
        // 1032번이면 시작 역(startName)에 따라 1032-1 또는 1032-2를 가져옴
        if (subwayCode == 1032) {
            if (isInRoute("1032-1", startName)) {
                return networks.getOrDefault("1032-1", new HashMap<>());
            } else if (isInRoute("1032-2", startName)) {
                return networks.getOrDefault("1032-2", new HashMap<>());
            }
        }
        // 일반적인 경우 subwayCode로 검색
        return networks.getOrDefault(String.valueOf(subwayCode), new HashMap<>());
    }

    // 특정 노선(routeName)에서 startName이 존재하는지 확인하는 메서드
    private boolean isInRoute(String routeName, String startName) {
        return networks.containsKey(routeName) && networks.get(routeName).containsKey(startName);
    }

    // 노선에 역을 추가
    private void addRoute(String line, String[] stations) {
        Map<String, List<String>> route = networks.get(line);
        for (int i = 0; i < stations.length - 1; i++) {
            String current = stations[i];
            String next = stations[i + 1];

            route.computeIfAbsent(current, k -> new ArrayList<>()).add(next);
            route.computeIfAbsent(next, k -> new ArrayList<>()).add(current);
        }
    }

    // 두 역 사이에 양방향 연결을 추가
    private void addLinkedConnection(String line, String station1, String station2) {
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
