package com.example.back.service.impl;

import com.example.back.dto.RouteResultDTO;
import com.example.back.dto.RouteDTO;
import com.example.back.service.RouteService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteServiceImpl implements RouteService {
    private final String baseURL = "https://api.odsay.com/v1/api/";
    @Value("${ODsay.apikey}")
    private String apiKey;

    @Override
    public List<RouteResultDTO> fetchAndProcessRoutes(RouteDTO routeDTO) {
        return null;
    }

}
