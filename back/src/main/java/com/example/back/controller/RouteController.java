package com.example.back.controller;

import com.example.back.dto.RealTimeResultDTO;
import com.example.back.dto.ResultDTO;
import com.example.back.dto.realtime.RealtimeDTO;
import com.example.back.dto.route.RouteDTO;
import com.example.back.service.RealLocationService;
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

    @Autowired
    private RealLocationService realLocationService;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/route")
    public ResponseEntity<List<ResultDTO>> getRoute(@RequestBody RouteDTO routeDTO) {
        List<ResultDTO> response = routeService.fetchAndProcessRoutes(routeDTO);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/realTime")
    public ResponseEntity<RealTimeResultDTO> getRealBusRoute(@RequestBody RealtimeDTO realtimeDTO)  {
        System.out.println("Received RealtimeDTO: " + realtimeDTO);
        RealTimeResultDTO response = realLocationService.getRealTime(realtimeDTO);
        return ResponseEntity.ok(response);
    }
//    @PostMapping("/DBSaveRoute")
}
