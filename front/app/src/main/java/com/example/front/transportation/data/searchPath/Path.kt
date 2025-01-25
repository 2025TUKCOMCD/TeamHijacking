package com.example.front.transportation.data.searchPath

data class Path(
    val pathType : Int,
    val info: Info,
    val subPath: List<SubPath>
)
