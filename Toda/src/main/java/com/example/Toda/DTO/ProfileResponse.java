package com.example.Toda.DTO;

public record ProfileResponse(
        String profilePhoto,
        String firstName,
        String lastName,
        String city,
        String email,
        String phone,
        String title

) {
}
