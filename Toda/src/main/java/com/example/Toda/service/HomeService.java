package com.example.Toda.service;

import com.example.Toda.DTO.BookingRequestResponse;
import com.example.Toda.DTO.HomeDashboardResponse;
import com.example.Toda.Entity.BookingRequest;
import com.example.Toda.Entity.UserEntity;
import com.example.Toda.repo.BookingRequestRepository;
import com.example.Toda.repo.TourGuideRepo;
import com.example.Toda.repo.TripRepository;
import com.example.Toda.repo.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
@Service
public class HomeService {
    @Autowired private TourGuideRepo guideRepo;
    @Autowired private TripRepository tripRepo;
    @Autowired private BookingRequestRepository requestRepo;
    @Autowired private UserRepo userRepo;

    public HomeDashboardResponse getDashboardDataByEmail(String email, HttpServletRequest request) {
        UserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getTourGuide() == null) {
            throw new RuntimeException("User is not a Tour Guide");
        }

        Long guideId = user.getTourGuide().getId();
        var guide = guideRepo.findById(guideId).orElseThrow();

        // 1. بناء الـ Base URL (زي الـ lastCreated)
        String baseUrl = request.getScheme() + "://" + request.getServerName();
        if (request.getServerPort() != 80 && request.getServerPort() != 443) {
            baseUrl += ":" + request.getServerPort();
        }

        // حل مشكلة الـ Lambda في الـ IDE: بنعمل نسخة final من الـ baseUrl
        final String finalBaseUrl = baseUrl;

        // 2. صورة المرشد (Profile Photo)
        String fullProfilePath = formatImageUrl(finalBaseUrl, guide.getProfilePhoto(), "profile-photos");

        // 3. صور الرحلات (Upcoming Trips)
        var trips = tripRepo.findByTourGuideIdOrderByStartDateAsc(guideId)
                .stream().map(t -> new HomeDashboardResponse.TripResponse(
                        t.getId(),
                        t.getTitle(),
                        formatImageUrl(finalBaseUrl, t.getTripCoverImage(), "covers"),
                        t.getStartDate() != null ? t.getStartDate().toString() : null,
                        t.getCity()))
                .limit(5).toList();

        // 4. صور السياح في الطلبات (Recent Requests)
        var requests = requestRepo.findByTourGuideIdAndStatus(guideId, BookingRequest.RequestStatus.PENDING)
                .stream().limit(2).map(req -> convertToResponse(req, finalBaseUrl)).toList();

        long completed = requestRepo.countByTourGuideIdAndStatus(guideId, BookingRequest.RequestStatus.COMPLETED);
        var stats = new HomeDashboardResponse.MonthlyStats(completed, 4.8, 1200.0);

        return new HomeDashboardResponse(
                guide.getName(),
                guide.getCity(),
                fullProfilePath,
                trips,
                requests,
                stats
        );
    }

    private BookingRequestResponse convertToResponse(BookingRequest entity, String baseUrl) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, h:mm a");
        String formattedDate = entity.getDate() != null ? entity.getDate().format(formatter) : "";

        // استخدام نفس المنطق لصور السياح
        String touristPhoto = formatImageUrl(baseUrl, entity.getTourist().getProfilePhoto(), "profile-photos");

        return new BookingRequestResponse(
                entity.getId(),
                entity.getTourist().getUser().getUsername(),
                touristPhoto,
                entity.getCategory(),
                formattedDate,
                entity.getTouristCount(),
                entity.getStatus().name()
        );
    }

    private String formatImageUrl(String baseUrl, String dbImageName, String folderName) {
        if (dbImageName == null || dbImageName.trim().isEmpty()) {
            return null;
        }

        String fileName = dbImageName.trim();

        // لو الداتا بيز فيها اللينك كامل، رجعه زي ما هو
        if (fileName.startsWith("http")) {
            return fileName;
        }

        // تنظيف المسار من أي "/" أو "\" عشان نجيب اسم الملف بس
        if (fileName.contains("/") || fileName.contains("\\")) {
            int lastSlash = Math.max(fileName.lastIndexOf("/"), fileName.lastIndexOf("\\"));
            fileName = fileName.substring(lastSlash + 1);
        }

        // بناء اللينك النهائي (Style: lastCreated)
        return baseUrl + "/uploads/" + folderName + "/" + fileName + "?t=" + System.currentTimeMillis();
    }
}