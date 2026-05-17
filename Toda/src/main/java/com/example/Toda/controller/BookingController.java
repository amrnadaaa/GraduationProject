package com.example.Toda.controller;

import com.example.Toda.DTO.ApiResponse;
import com.example.Toda.DTO.BookingRequestResponse;
import com.example.Toda.Entity.BookingRequest;
import com.example.Toda.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/requests")

public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<BookingRequestResponse>>> getPendingRequests(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<BookingRequestResponse> responses = bookingService.getRequestsByStatus(
                userDetails.getUsername(),
                BookingRequest.RequestStatus.PENDING
        );
        return ResponseEntity.ok(ApiResponse.success("Pending requests retrieved", responses));
    }

    @GetMapping("/accepted")
    public ResponseEntity<ApiResponse<List<BookingRequestResponse>>> getAcceptedRequests(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<BookingRequestResponse> responses = bookingService.getRequestsByStatus(
                userDetails.getUsername(),
                BookingRequest.RequestStatus.ACCEPTED
        );
        return ResponseEntity.ok(ApiResponse.success("Accepted requests retrieved", responses));
    }

    @PatchMapping("/{requestId}/accept")
    public ResponseEntity<ApiResponse<String>> acceptRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails) {

        bookingService.updateStatus(requestId, BookingRequest.RequestStatus.ACCEPTED, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Trip successfully confirmed", null));
    }

    @PatchMapping("/{requestId}/decline")
    public ResponseEntity<ApiResponse<String>> declineRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails) {

        bookingService.updateStatus(requestId, BookingRequest.RequestStatus.DECLINED, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Trip request declined successfully", null));
    }
    @GetMapping("/declined")
    public ResponseEntity<ApiResponse<List<BookingRequestResponse>>> getDeclinedRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<BookingRequestResponse> responses = bookingService.getRequestsByStatus(
                userDetails.getUsername(),
                BookingRequest.RequestStatus.DECLINED
        );
        return ResponseEntity.ok(ApiResponse.success("Declined requests retrieved", responses));
    }
}
