package com.example.Toda.controller;

import com.example.Toda.DTO.HomeDashboardResponse;
import com.example.Toda.service.HomeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/home")
public class HomeController {

    @Autowired
    private HomeService homeService;

    @GetMapping("/dashboard")
    public ResponseEntity<HomeDashboardResponse> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        String email = userDetails.getUsername();

        return ResponseEntity.ok(homeService.getDashboardDataByEmail(email, request));
    }
}
