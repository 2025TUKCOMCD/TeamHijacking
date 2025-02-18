package com.example.back.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SubwayApi {
    @GET("{apiKey}/json/realtimeStationArrival/0/5/{stationName}")
    Call<ResponseBody> getRealtimeStationArrival(
            @Path("apiKey") String apiKey,
            @Path("stationName")String stationName
    );
}
