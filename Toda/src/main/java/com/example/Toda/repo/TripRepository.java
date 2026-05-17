package com.example.Toda.repo;

import com.example.Toda.Entity.TourGuideEntity;
import com.example.Toda.Entity.Trip;
import com.example.Toda.Entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByTourGuideIdOrderByStartDateAsc(Long guideId);

    List<Trip> findByTourGuideIdAndStartDateAfterOrderByStartDateAsc(Long guideId, LocalDate date);
    @Query("SELECT t FROM Trip t WHERE t.tourGuide.email = :email " +
            "AND (:status IS NULL OR t.status = :status)")
    List<Trip> findByEmailAndOptionalStatus(
            @Param("email") String email,
            @Param("status") TripStatus status);
   // Optional<Trip> findFirstByTourGuideOrderByCreatedAtDesc(TourGuideEntity tourGuide);
    Optional<Trip> findFirstByTourGuideOrderByIdDesc(TourGuideEntity tourGuide);

}