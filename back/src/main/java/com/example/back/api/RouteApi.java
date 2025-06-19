package com.example.back.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RouteApi {
    @GET("searchPubTransPathT")
    Call<ResponseBody> searchPubTransPathT(
            @Query("SX") double startLng,
            @Query("SY") double startLat,
            @Query("EX") double endLng,
            @Query("EY") double endLat,
            @Query("apiKey") String apiKey
    );

    @GET("busLaneDetail")
    Call<ResponseBody> busLaneDetail(
            @Query("busID") int busID,
            @Query("apiKey") String apiKey
    );
}
