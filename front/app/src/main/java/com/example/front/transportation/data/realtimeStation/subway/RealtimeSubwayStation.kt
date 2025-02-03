package com.example.front.transportation.data.realtimeStation.subway

data class RealtimeSubwayStation(
    val RESULT: Result,
    val row: List<Row>
)

data class Result(
    val code: String,
    val developerMessage: String?,
    val link: String?,
    val message: String,
    val status: Int,
    val total: Int
)

data class Row(
    val rowNum: Int,
    val selectedCount: Int,
    val totalCount: Int,
    val subwayId: Int,
    val updnLine: String,
    val trainLineNm: String,
    val statnFid: Long,
    val statnTid: Long,
    val statnId: Long,
    val statnNm: String,
    val trnsitCo: Int,
    val ordkey: String,
    val subwayList: String,
    val statnList: String,
    val btrainSttus: String,
    val barvlDt: Int,
    val btrainNo: String,
    val bstatnId: Long,
    val bstatnNm: String,
    val recptnDt: String,
    val arvlMsg2: String,
    val arvlMsg3: String,
    val arvlCd: Int,
    val lstcarAt: Int
)