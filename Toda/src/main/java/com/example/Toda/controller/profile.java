package com.example.Toda.controller;

import com.example.Toda.DTO.ApiResponse;
import com.example.Toda.DTO.ChangePasswordRequest;
import com.example.Toda.DTO.ProfileResponse;
import com.example.Toda.DTO.updateProfileRequest;
import com.example.Toda.service.forgetPasswordService;
import com.example.Toda.service.profileService;
import com.example.Toda.service.restPasswordService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
public class profile {
      private final profileService profileService;

    public profile(profileService profileService) {
        this.profileService = profileService;
    }


    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<String>> updateProfile(@AuthenticationPrincipal UserDetails userDetails,@RequestBody updateProfileRequest request ) {
        String email = userDetails.getUsername();
        profileService.updateProfile(email,request);
        return ResponseEntity.ok().body(ApiResponse.success("profile updated successfully",null));
    }

    @PostMapping("/check-password")
    public ResponseEntity<ApiResponse<String>> checkPassword(@RequestBody String password, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        profileService.checkPassword(email,password);
        return ResponseEntity.ok().body(ApiResponse.success("password check successfully",null));

    }
    @DeleteMapping("/delete-account")
    public ResponseEntity<ApiResponse<String>> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        profileService.DeleteAccount(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("your Account was deleted", null));
    }
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        return ResponseEntity.ok(profileService.getProfileDataByEmail(userDetails.getUsername(), request));
    }

}
