package com.example.front.transportation.data.realTime


data class RealtimeDTO(
    val type: Int = 0, // 지하철(1), 버스(2), 도보(3) 여부
    val boarding: Int = 0, // 탑승 여부(1: 탑승 전, 2: 탑승 후, 3: 탑승 중)
    val transportLocalID: Int = 0, // 대중교통 : 노선/역 ID (busRouteId, lineId)
    val dbUsage: Int = 0, // DB 사용 여부 (0: 사용 안함, 1: 사용함)
    val stationId: Int = 0, // 버스 : 정류장 ID (stid)
    val startOrd: Int = 0, // 버스 : 위치 정보 첫번째 순서
    val endOrd: Int = 0, // 버스 : 위치 정보 마지막 순서
    val trainNo: String? = null, // 지하철 : 기차 번호
    val vehid: String? = "0", // 버스 : 차량 ID (vehId)
    val startName: String? = null, // 지하철 : 지하철 역 이름
    val secondName: String? = null, // 지하철 : 두번째 지하철 역 이름
    val endName: String? = null, // 지하철 : 지하철 역 이름
    val direction: String? = null, // 지하철 : 방향 정보
    val location : String? = null // 현재 위치 (예: "서울역", "강남역" 등, 지하철/버스/도보 모두 사용 가능)

)

