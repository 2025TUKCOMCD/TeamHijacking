package com.example.back.controller;

import com.example.back.dto.RouteDTO;
import com.example.back.dto.RouteResultDTO;
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
        List<RouteResultDTO> response = routeService.fetchAndProcessRoutes(routeDTO);
        return ResponseEntity.ok(response);
    }
}
