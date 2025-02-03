package com.example.back.service;

public class RouteService {
        List<PathRouteResult> fetchAndProcessRoutes(double startLat, double startLng, double endLat, double endLng);
        List<Map<String, String>> fetchBusRouteDetails(int busID, int startStationID, int endStationID, String startLocalStationID, String endLocalStationID);
        double calculateRouteScore(Path path)
}
