package com.example.back.api;

import com.example.back.dto.subway.arrive.SubwayArriveProcessDTO;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SubwayApi {
    @GET("{apiKey}/json/realtimeStationArrival/0/1000/{stationName}")
    Call<ResponseBody> getRealtimeStationArrival(
            @Path("apiKey") String apiKey,
            @Path("stationName")String stationName
    );
}
