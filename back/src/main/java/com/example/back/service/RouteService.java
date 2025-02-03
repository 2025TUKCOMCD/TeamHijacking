package com.example.back.service;

import com.example.back.dto.RouteResultDTO;
import com.example.back.dto.RouteDTO;


import java.util.List;

public interface RouteService {
    List<RouteResultDTO> fetchAndProcessRoutes(RouteDTO routeDTO);
}
