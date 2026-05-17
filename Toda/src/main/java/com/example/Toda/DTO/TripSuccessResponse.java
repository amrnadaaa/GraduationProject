package com.example.Toda.DTO;

import java.time.LocalDate;
import java.util.List;

public record TripSuccessResponse(
        Long id,
        String title,
        String city,
        Double price,
        String tripCoverImage,
        LocalDate startDate,
        LocalDate endDate,
        List<String> categories,
        String status

) {
}
