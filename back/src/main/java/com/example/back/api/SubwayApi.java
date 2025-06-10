package com.example.back.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SubwayApi {
    // 지하철 실시간 도착정보
    @GET("{apiKey}/json/realtimeStationArrival/0/1000/{stationName}")
    Call<ResponseBody> getRealtimeStationArrival(
            @Path("apiKey") String apiKey,
            @Path("stationName")String stationName
    );
    // 지하철 실시간 위치정보
    @GET("{apiKey}/json/realtimePosition/0/1000/{routeName}")
    Call<ResponseBody> getRealtimePosition(
            @Path("apiKey") String apiKey,
            @Path("routeName") String routeName
    );
}
