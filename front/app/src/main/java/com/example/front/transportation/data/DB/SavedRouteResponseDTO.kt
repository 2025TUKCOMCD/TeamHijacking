package com.example.front.transportation.data.DB

import com.google.gson.annotations.SerializedName

data class SavedRouteResponseDTO(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("savedRouteName") val savedRouteName: String?, // 서버 응답에 'savedRouteId'가 없음 (선택적)
    @SerializedName("savedRouteId") val savedRouteId: Int?,       // 서버 응답에 'savedRouteId'가 포함될 수 있음 (선택적)
    @SerializedName("alreadyExists") val alreadyExists: Boolean
)
