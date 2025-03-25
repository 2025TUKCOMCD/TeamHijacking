package com.example.back.service;

import com.example.back.dto.route.RouteProcessDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RouteCalService {
    // 경로 점수 계산
    public double calculateRouteScore(RouteProcessDTO.Path path) {
        RouteProcessDTO.Info info = path.getInfo();

        int totalTransfers = info.getBusTransitCount() + info.getSubwayTransitCount();
        double transferScore = totalTransfers > 0 ? Math.pow(1.5, totalTransfers) : 1.0;

        double timeScore = (info.getTotalTime() / 60.0) * 1.0;
        double busWeight = totalTransfers > 0 ? (double) info.getBusTransitCount() / totalTransfers : 0.0;

        double walkFactor = (info.getTotalWalk() / 1000.0) * 1.0;

        return (0.4 * transferScore) +
                (0.3 * timeScore) +
                (0.2 * busWeight) +
                (0.1 * walkFactor);
    }

    // 경로 대중교통 정보(지하철, 버스, 버스+지하철)
    public String mapTransitType(int pathType) {
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

    // 경로 대중교통 정보(지하철, 버스, 도보)
    public List<Integer> mapPath(List<RouteProcessDTO.SubPath> subPaths) {
        return subPaths.stream()
                .map(subPath -> {
                    switch (subPath.getTrafficType()) {
                        case 1:
                            return 1;
                        case 2:
                            return 2;
                        case 3:
                            return subPath.getSectionTime() > 0 ? 3 : null;
                        default:
                            return null;
                    }
                })
                .filter(detail -> detail != null)
                .collect(Collectors.toList());
    }
    public List<String> mapDetailedTrans(List<RouteProcessDTO.SubPath> subPaths) {
        return subPaths.stream()
                .map(subPath -> {
                    switch (subPath.getTrafficType()) {
                        case 1:
                            return subPath.getLane().get(0).getName();
                        case 2:
                            return subPath.getLane().get(0).getBusNo() + "번";
                        case 3:
                            return subPath.getSectionTime() > 0 ? "도보" : "";
                        default:
                            return "알 수 없는 교통수단";
                    }
                })
                .filter(detail -> !detail.isEmpty())
                .collect(Collectors.toList());
    }
}
