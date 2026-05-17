package com.example.Toda.service;

import com.example.Toda.DTO.*;
import com.example.Toda.Entity.TourGuideEntity;
import com.example.Toda.Entity.Trip;
import com.example.Toda.Entity.TripStatus;
import com.example.Toda.Entity.UserEntity;
import com.example.Toda.mapper.TripMapper;
import com.example.Toda.repo.TripRepository;
import com.example.Toda.repo.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class TripService {
    private final TripRepository tripRepository;
    private final UserRepo userRepo;

    @Autowired
    private TripMapper tripMapper;

    private final String uploadDir = "uploads/covers/";

    public TripService(TripRepository tripRepository, UserRepo userRepo) {
        this.tripRepository = tripRepository;
        this.userRepo = userRepo;
    }


    public Integer createCustomTrip(TripBasicInfoRequest request, String username) {
        UserEntity user = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getTourGuide() == null) {
            throw new RuntimeException("This user is not registered as a Tour Guide");
        }

        Trip trip = tripMapper.toEntity(request);
        trip.setTourGuide(user.getTourGuide());
        trip.setStatus(TripStatus.NEW);

        Trip savedTrip = tripRepository.save(trip);

        return Math.toIntExact(savedTrip.getId());
    }


    public void addTripTime(TripInfoTimeRequest request, String username) {
        Trip lastTrip = getLatestTripInternal(username);

        lastTrip.setStartDate(request.startDate());
        lastTrip.setEndDate(request.endDate());

        tripRepository.save(lastTrip);
    }

    public void addTripPrice(TripInfoPriceRequest request, String username) {
        Trip lastTrip = getLatestTripInternal(username);

        lastTrip.setPricePerTourist(request.pricePerTourist());

        tripRepository.save(lastTrip);
    }

    public String uploadTripCover(Long tripId, String email, MultipartFile file) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (!trip.getTourGuide().getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }

        try {
            String originalName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileName = UUID.randomUUID() + "_" + originalName;

            Path path = Paths.get(uploadDir + fileName);

            Files.createDirectories(path.getParent());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);


            String dbPath = "/uploads/" + fileName;
            trip.setTripCoverImage(dbPath);

            tripRepository.save(trip);

            return dbPath;

        } catch (IOException e) {
            throw new RuntimeException("Could not store file", e);
        }
    }

    public List<TripCardResponse> getFilteredTrips(String email, String status) {
        List<Trip> trips = tripRepository.findByEmailAndOptionalStatus(email, TripStatus.valueOf(status));
        return tripMapper.toCardResponseList(trips);
    }

    public TripSuccessResponse getLatestCreatedTrip(String email, HttpServletRequest request) {
        Trip lastTrip = getLatestTripInternal(email);

        String baseUrl = request.getScheme() + "://" + request.getServerName();
        if (request.getServerPort() != 80 && request.getServerPort() != 443) {
            baseUrl += ":" + request.getServerPort();
        }

        String fullImageUrl = null;
        if (lastTrip.getTripCoverImage() != null) {
            String fileName = lastTrip.getTripCoverImage();


            if (fileName.contains("/") || fileName.contains("\\")) {
                fileName = java.nio.file.Paths.get(fileName).getFileName().toString();
            }


            fullImageUrl = baseUrl + "/uploads/covers/" + fileName + "?t=" + System.currentTimeMillis();
        }

        return new TripSuccessResponse(
                lastTrip.getId(),
                lastTrip.getTitle(),
                lastTrip.getCity(),
                lastTrip.getPricePerTourist(),
                fullImageUrl,
                lastTrip.getStartDate(),
                lastTrip.getEndDate(),
                lastTrip.getCategories(),
                lastTrip.getStatus() != null ? lastTrip.getStatus().name() : "NEW"
        );
    }

    private Trip getLatestTripInternal(String email) {
        UserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getTourGuide() == null) {
            throw new RuntimeException("User is not registered as a Tour Guide");
        }

        return tripRepository.findFirstByTourGuideOrderByIdDesc(user.getTourGuide())
                .orElseThrow(() -> new RuntimeException("No trip found! Start by creating basic info."));
    }
}

