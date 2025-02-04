package com.example.back.service;

import com.example.back.dto.route.RouteResultDTO;
import com.example.back.dto.route.RouteDTO;


import java.util.List;

public interface RouteService {
    List<RouteResultDTO> fetchAndProcessRoutes(RouteDTO routeDTO);
}
