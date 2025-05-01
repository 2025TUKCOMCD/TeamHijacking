package com.example.back.controller;

import com.example.back.dto.ResultDTO;
import com.example.back.dto.route.RouteDTO;
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

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/route")
    public ResponseEntity<List<ResultDTO>> getRoute(@RequestBody RouteDTO routeDTO) {
        System.out.println("🔍 요청 도착: " + routeDTO);
        List<ResultDTO> response = routeService.fetchAndProcessRoutes(routeDTO);
        return ResponseEntity.ok(response);
    }
}
