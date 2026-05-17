package com.example.Toda.service;

import com.example.Toda.DTO.ProfileResponse;
import com.example.Toda.DTO.updateProfileRequest;
import com.example.Toda.Entity.TourGuideEntity;
import com.example.Toda.Entity.UserEntity;
import com.example.Toda.repo.TourGuideRepo;
import com.example.Toda.repo.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

@Service
public class profileService {
    private final UserRepo userRepo;
    private final TourGuideRepo tourGuideRepo;

    public profileService(UserRepo userRepo, TourGuideRepo tourGuideRepo) {
        this.userRepo = userRepo;
        this.tourGuideRepo = tourGuideRepo;
    }

    @Transactional
    public void updateProfile(String currentEmail, updateProfileRequest request) {
        UserEntity user = userRepo.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String fullName = request.firstName() + " " + request.lastName();
        user.setUsername(fullName);
        user.setEmail(request.email());

        TourGuideEntity guide = user.getTourGuide();
        if (guide != null) {
            guide.setName(fullName);
            guide.setPhone(request.phone());
            guide.setEmail(request.email());
        }
    }

    public void changeTourguidePassword(String email) {
        UserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void checkPassword(String email, String password) {
        UserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Passwords don't match");
        }
    }

    @Transactional
    public void DeleteAccount(String username) {
        UserEntity user = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
          userRepo.delete(user);

    }
    public ProfileResponse getProfileDataByEmail(String email, HttpServletRequest request) {
        UserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getTourGuide() == null) {
            throw new RuntimeException("User is not a Tour Guide");
        }

        var guide = user.getTourGuide();


        String baseUrl = request.getScheme() + "://" + request.getServerName();
        if (request.getServerPort() != 80 && request.getServerPort() != 443) {
            baseUrl += ":" + request.getServerPort();
        }


        String fullProfilePath = formatImageUrl(baseUrl, guide.getProfilePhoto(), "profile-photos");

        String fullName = user.getUsername() != null ? user.getUsername().trim() : "";
        String firstName = fullName;
        String lastName = "";

        if (fullName.contains(" ")) {
            int i = fullName.indexOf(" ");
            firstName = fullName.substring(0, i);
            lastName = fullName.substring(i + 1);
        }

        return new ProfileResponse(
                fullProfilePath,
                firstName,
                lastName,
                guide.getCity(),
                user.getEmail(),
                guide.getPhone(),
                "Licensed Guide"

        );
    }

    private String formatImageUrl(String baseUrl, String dbImageName, String folderName) {
        if (dbImageName == null || dbImageName.trim().isEmpty()) {
            return null;
        }

        String fileName = dbImageName.trim();

        if (fileName.contains("/") || fileName.contains("\\")) {
            try {
                fileName = java.nio.file.Paths.get(fileName).getFileName().toString();
            } catch (Exception e) {
                int lastSlash = Math.max(fileName.lastIndexOf("/"), fileName.lastIndexOf("\\"));
                if (lastSlash != -1) {
                    fileName = fileName.substring(lastSlash + 1);
                }
            }
        }

        return baseUrl + "/uploads/" + folderName + "/" + fileName + "?t=" + System.currentTimeMillis();
    }
}