package com.example.back.controller;

import com.example.back.dto.route.RouteDTO;
import com.example.back.dto.route.RouteResultDTO;
import com.example.back.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @PostMapping("/route")
    public ResponseEntity<List<RouteResultDTO>> getRoute(@RequestBody RouteDTO routeDTO) {
        System.out.println("🔍 요청 도착: " + routeDTO);
        List<RouteResultDTO> response = routeService.fetchAndProcessRoutes(routeDTO);
        System.out.println("📦 응답: " + response);
        return ResponseEntity.ok(response);
    }
}
