package com.example.back.service;

import com.example.back.api.BusApi;
import com.example.back.api.RouteApi;
import com.example.back.api.SubwayApi;
import com.example.back.dto.bus.arrive.BusArriveProcessDTO;
import com.example.back.dto.bus.detail.BusDetailProcessDTO;
import com.example.back.dto.bus.realtime.RealBusLocationDTO;
import com.example.back.dto.route.RouteDTO;
import com.example.back.dto.route.RouteProcessDTO;
import com.example.back.dto.subway.arrive.SubwayArriveProcessDTO;
import com.example.back.dto.subway.realtime.RealSubwayLocationDTO;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class APIService {
    private final String ODsayBaseURL = "https://api.odsay.com/v1/api/";
    private final String Seoul_BusBaseURL = "http://ws.bus.go.kr/api/rest/";
    private final String Seoul_SubwayBaseURL = "http://swopenAPI.seoul.go.kr/api/subway/";

    @Value("${ODsay.apikey}")
    private String Odsay_apiKey;

    @Value(("${Public_Bus_APIKEY}"))
    private String Seoul_Bus_apiKey;

    @Value(("${Public_Subway_APIKEY}"))
    private String Seoul_Subway_apiKey;

    private final Gson gson = new Gson();
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private final Retrofit OdsayRetrofit = new Retrofit.Builder()
            .baseUrl(ODsayBaseURL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    private final Retrofit BusRetrofit = new Retrofit.Builder()
            .baseUrl(Seoul_BusBaseURL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    private final Retrofit SubwayRetrofit = new Retrofit.Builder()
            .baseUrl(Seoul_SubwayBaseURL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    private final RouteApi routeApi = OdsayRetrofit.create(RouteApi.class);
    private final BusApi busApi = BusRetrofit.create(BusApi.class);
    private final SubwayApi subwayApi = SubwayRetrofit.create(SubwayApi.class);


    // Odsay 경로 조회
    public RouteProcessDTO.SearchPath fetchAndProcessRoutes(RouteDTO routeDTO) throws IOException {
        Call<ResponseBody> call = routeApi.searchPubTransPathT(
                routeDTO.getStartLat(),
                routeDTO.getStartLng(),
                routeDTO.getEndLat(),
                routeDTO.getEndLng(),
                Odsay_apiKey
        );

        ResponseBody responseBody = call.execute().body();
        if (responseBody != null) {
            String rawJson = responseBody.string();
            RouteProcessDTO.SearchPath parsedResponse = gson.fromJson(rawJson, RouteProcessDTO.SearchPath.class);

            return parsedResponse != null ? parsedResponse : new RouteProcessDTO.SearchPath();
        } else {
            return new RouteProcessDTO.SearchPath();
        }
    }

    // Odsay 버스 상세 정보 조회
    public BusDetailProcessDTO.BusLaneDetail fetchBusLaneDetail(int busID) throws IOException {
        Call<ResponseBody> call = routeApi.busLaneDetail(busID, Odsay_apiKey);
        ResponseBody responseBody = call.execute().body();

        if (responseBody != null) {
            String rawJson = responseBody.string();
            BusDetailProcessDTO.BusLaneDetail parsedResponse = gson.fromJson(rawJson, BusDetailProcessDTO.BusLaneDetail.class);

            return parsedResponse != null ? parsedResponse : new BusDetailProcessDTO.BusLaneDetail();
        } else {
            return new BusDetailProcessDTO.BusLaneDetail();
        }
    }

    // 버스 도착 정보 조회
    public BusArriveProcessDTO.arriveDetail fetchAndBusArrive(int stId, int busRouteId, int ord) throws IOException {
        Call<ResponseBody> call = busApi.getArrInfoByRoute(
                Seoul_Bus_apiKey,
                stId,
                busRouteId,
                ord,
                "json"
        );
        ResponseBody responseBody = call.execute().body();
        if (responseBody != null) {
            String rawJson = responseBody.string();
            BusArriveProcessDTO.arriveDetail parsedResponse = gson.fromJson(rawJson, BusArriveProcessDTO.arriveDetail.class);

            // 리스트 형태로 반환
            return parsedResponse != null ? parsedResponse : new BusArriveProcessDTO.arriveDetail();
        } else {
            return new BusArriveProcessDTO.arriveDetail();
        }
    }

    // 서울 지하철 도착 정보 조회
    public SubwayArriveProcessDTO fetchAndSubwayArrive(String stationName) throws IOException {
        Call<ResponseBody> call = subwayApi.getRealtimeStationArrival(Seoul_Subway_apiKey, stationName);
        ResponseBody responseBody = call.execute().body(); // 동기 호출
        if (responseBody != null) {
            String rawJson = responseBody.string();
            SubwayArriveProcessDTO subwayArriveProcessDTO = gson.fromJson(rawJson, SubwayArriveProcessDTO.class);
            // 도착 정보 리스트 반환
            return subwayArriveProcessDTO != null ? subwayArriveProcessDTO : new SubwayArriveProcessDTO();
        } else {
            return new SubwayArriveProcessDTO();
        }

    }

    // 버스 실시간 위치 정보 조회
    public RealBusLocationDTO fetchAndBusLocation(String vehid) throws IOException {
        Call<ResponseBody> call = busApi.getBusPosByVehId(
                Seoul_Bus_apiKey,
                vehid,
                "json"
        );
        ResponseBody responseBody = call.execute().body();
        if (responseBody != null) {
            String rawJson = responseBody.string();
            RealBusLocationDTO parsedResponse = gson.fromJson(rawJson, RealBusLocationDTO.class);

            return parsedResponse != null ? parsedResponse : new RealBusLocationDTO();
        } else {
            return new RealBusLocationDTO();
        }
    }

    // 서울 지하철 실시간 위치 정보 조회
    public RealSubwayLocationDTO fetchAndSubwayLocation(String routeName) throws IOException {
        Call<ResponseBody> call = subwayApi.getRealtimePosition(Seoul_Subway_apiKey, routeName);
        ResponseBody responseBody = call.execute().body(); // 동기 호출
        if (responseBody != null) {
            String rawJson = responseBody.string();
            RealSubwayLocationDTO parsedResponse = gson.fromJson(rawJson, RealSubwayLocationDTO.class);
            // 도착 정보 리스트 반환
            return parsedResponse != null ? parsedResponse : new RealSubwayLocationDTO();
        } else {
            return new RealSubwayLocationDTO();
        }
    }
}
