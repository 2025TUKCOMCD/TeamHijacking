package com.example.front.data.searchPath

data class SubPath(
    val trafficType: Int,
    val distance: Double?,
    val startName: String?,
    val endName: String?,
    val lane: List<Lane>?,
    val sectionTime: Int?
)