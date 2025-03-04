package com.example.front.transportation.data.searchPath

import android.os.Parcel
import android.os.Parcelable

data class RouteRequest(
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double
)

data class Route(
    val totalTime: Int,
    val transitCount: Int,
    val mainTransitType: String,
    val pathTransitType: List<Int>,
    val transitTypeNo: List<String>,
    val routeIds: List<RouteId>
)

data class RouteId(
    val busLocalBlID: List<Int>,
    val startStationInfo: Int,
    val endStationInfo: Int,
    val stationInfo: List<Int>,
    val predictTimes1: List<String>,
    val predictTimes2: List<String>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createIntArray()?.toList() ?: emptyList(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.createIntArray()?.toList() ?: emptyList(),
        parcel.createStringArrayList() ?: emptyList(),
        parcel.createStringArrayList() ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeIntArray(busLocalBlID.toIntArray())
        parcel.writeInt(startStationInfo)
        parcel.writeInt(endStationInfo)
        parcel.writeIntArray(stationInfo.toIntArray())
        parcel.writeStringList(predictTimes1)
        parcel.writeStringList(predictTimes2)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RouteId> {
        override fun createFromParcel(parcel: Parcel): RouteId {
            return RouteId(parcel)
        }

        override fun newArray(size: Int): Array<RouteId?> {
            return arrayOfNulls(size)
        }
    }
}