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

    @GET("6410000/busarrivalservice/v2/getBusArrivalItemv2")
    Call<ResponseBody> getBusArrivalItemv2(
            @Query("serviceKey") String serviceKey,
            @Query("stationId") int stationId,
            @Query("routeId") int routeId,
            @Query("staOrder") int staOrder,
            @Query("format") String format
    );
}
