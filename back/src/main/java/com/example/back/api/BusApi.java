package com.example.back.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BusApi {
    @GET("arrive/getArrInfoByRoute")
    Call<ResponseBody> getArrInfoByRoute(
            @Query("ServiceKey") String ServiceKey,
            @Query("stId") int stId,
            @Query("busRouteId") int busRouteId,
            @Query("ord") int ord,
            @Query("resultType") String resultType
    );

    @GET("buspos/getBusPosByRouteSt")
    Call<ResponseBody> getBusPosByRouteSt(
            @Query("ServiceKey")String ServiceKey,
            @Query("busRouteId")int busRouteId,
            @Query("startOrd")int startOrd,
            @Query("endOrd")int endOrd,
            @Query("resultType")String resultType
    );


}
