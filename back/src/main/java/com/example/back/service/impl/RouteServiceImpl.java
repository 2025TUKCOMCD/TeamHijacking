package com.example.back.service.impl;

import com.example.back.api.RouteApi;
import com.example.back.dto.route.*;
import com.example.back.service.RouteService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RouteServiceImpl implements RouteService {
    private final String baseURL = "https://api.odsay.com/v1/api/";
    @Value("${ODsay.apikey}")
    private String Odsay_apiKey;

    private final Gson gson = new Gson();
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(baseURL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    private final RouteApi routeApi = retrofit.create(RouteApi.class);

    @Override
    public List<RouteResultDTO> fetchAndProcessRoutes(RouteDTO routeDTO) {
        try {
            Call<ResponseBody> call = routeApi.searchPubTransPathT(
                    routeDTO.getStartLat(),
                    routeDTO.getStartLng(),
                    routeDTO.getEndLat(),
                    routeDTO.getEndLng(),
                    Odsay_apiKey
            );
            System.out.println("Request URL: " + call.request().url());
            System.out.println("Request Parameters: " + call.request().url().query());

            ResponseBody responseBody = call.execute().body();
            if (responseBody != null) {
                String rawJson = responseBody.string();
                System.out.println(rawJson);

                // JSON 응답을 파싱하여 RouteProcessDTO 객체 리스트로 변환
                RouteProcessDTO.SearchPath parsedResponse = gson.fromJson(rawJson, RouteProcessDTO.SearchPath.class);
                if (parsedResponse != null && parsedResponse.getResult() != null) {
                    List<RouteProcessDTO.Path> paths = parsedResponse.getResult().getPath();
                    if (paths != null) {
                        return paths.stream().map(this::mapToRouteResultDTO).collect(Collectors.toList());
                    }
                }
                return Collections.emptyList();
            } else {
                System.err.println("Error: Empty response body");
                return Collections.emptyList();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return Collections.emptyList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private RouteResultDTO mapToRouteResultDTO(RouteProcessDTO.Path path) {
        RouteResultDTO routeResultDTO = new RouteResultDTO();
        routeResultDTO.setTotalTime(path.getInfo().getTotalTime());
        routeResultDTO.setTransitCount(path.getInfo().getBusTransitCount() + path.getInfo().getSubwayTransitCount());
        routeResultDTO.setMainTransitType(mapTransitType(path.getPathType()));
        routeResultDTO.setDetailedPath(mapDetailedPath(path.getSubPath()));
        //routeResultDTO.setBusDetails(mapBusDetails(path.getSubPath()));
        return routeResultDTO;
    }

    private String mapTransitType(int pathType) {
        switch (pathType) {
            case 1:
                return "지하철";
            case 2:
                return "버스";
            case 3:
                return "버스+지하철";
            default:
                return "알 수 없음";
        }
    }

    private String mapDetailedPath(List<RouteProcessDTO.SubPath> subPaths) {
        return subPaths.stream()
                .map(subPath -> {
                    switch (subPath.getTrafficType()) {
                        case 1:
                            return subPath.getLane().get(0).getName() + " (" + subPath.getStartName() + "), (" + subPath.getEndName() + ")";
                        case 2:
                            return subPath.getLane().get(0).getBusNo() + "번 (" + subPath.getStartName() + "), (" + subPath.getEndName() + ")";
                        case 3:
                            return subPath.getSectionTime() > 0 ? "도보(" + subPath.getSectionTime() + "분)" : "";
                        default:
                            return "알 수 없는 교통수단";
                    }
                })
                .filter(detail -> !detail.isEmpty())
                .collect(Collectors.joining(" -> "));
    }
}
