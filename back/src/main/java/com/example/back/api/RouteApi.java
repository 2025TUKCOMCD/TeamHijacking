package com.example.back.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RouteApi {
    @GET("searchPubTransPathT")
    Call<ResponseBody> searchPubTransPathT(
            @Query("SX") double startLat,
            @Query("SY") double startLng,
            @Query("EX") double endLat,
            @Query("EY") double endLng,
            @Query("apiKey") String apiKey
    );

    @GET("busLaneDetail")
    Call<ResponseBody> busLaneDetail(
            @Query("busID") int busID,
            @Query("apiKey") String apiKey
    );
}
