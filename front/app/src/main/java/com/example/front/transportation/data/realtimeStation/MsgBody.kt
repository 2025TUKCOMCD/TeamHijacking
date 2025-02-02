package com.example.front.transportation.data.realtimeStation

import com.google.gson.annotations.SerializedName

data class MsgBody(
    @SerializedName("busArrivalItem")
    val busArrivalItem: List<BusArrivalItem>? = null,

    @SerializedName("itemList")
    val itemList: List<Item>? = null
)
