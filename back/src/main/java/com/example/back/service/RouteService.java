package com.example.back.service;

import com.example.back.dto.ResultDTO;
import com.example.back.dto.route.RouteDTO;


import java.util.List;

public interface RouteService {
    List<ResultDTO> fetchAndProcessRoutes(RouteDTO routeDTO);

}
