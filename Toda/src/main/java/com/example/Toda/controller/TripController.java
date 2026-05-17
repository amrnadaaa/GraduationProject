package com.example.Toda.controller;

import com.example.Toda.DTO.*;
import com.example.Toda.service.TripService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trip")
public class TripController {
    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping("/create-basic")
    public ResponseEntity<ApiResponse<Integer>>createCustomTrip(@RequestBody TripBasicInfoRequest request,@AuthenticationPrincipal UserDetails userDetails){

     Integer tripId = tripService.createCustomTrip(request,userDetails.getUsername());
        return ResponseEntity.ok().body(ApiResponse.success("Created",tripId));



    }
    @PostMapping("/trip-time")
    public ResponseEntity<ApiResponse<String>>CustomTripTime(@RequestBody TripInfoTimeRequest request, @AuthenticationPrincipal UserDetails userDetails){

        tripService.addTripTime(request,userDetails.getUsername());
        return ResponseEntity.ok().body(ApiResponse.success("Added ",null));

    }
    @PostMapping("/trip-price")
    public ResponseEntity<ApiResponse<String>>CustomTripPrice(@RequestBody TripInfoPriceRequest request, @AuthenticationPrincipal UserDetails userDetails){

        tripService.addTripPrice(request,userDetails.getUsername());
        return ResponseEntity.ok().body(ApiResponse.success("Added ",null));

    }
    @PostMapping("/trips/{tripId}/upload-cover")
    public ResponseEntity<ApiResponse<String>> uploadTripCover(
            @PathVariable Long tripId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        String imageUrl = tripService.uploadTripCover(
                tripId,
                userDetails.getUsername(),
                file
        );

        return ResponseEntity.ok(
                ApiResponse.success("Image uploaded successfully", imageUrl)
        );
    }
    @GetMapping("/guideTrips")
    public ResponseEntity<ApiResponse<List<TripCardResponse>>> getGuideTrips(
            @RequestParam(required = false) String statusKey,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        List<TripCardResponse> trips = tripService.getFilteredTrips(email, statusKey);
        return ResponseEntity.ok(ApiResponse.success("Filtered trips fetched successfully", trips));
    }
    @GetMapping("/last-created")
    public ResponseEntity<ApiResponse<TripSuccessResponse>> getLastTrip(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) { // ضيف ده هنا

        TripSuccessResponse data = tripService.getLatestCreatedTrip(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Success screen data fetched", data));
    }
}
