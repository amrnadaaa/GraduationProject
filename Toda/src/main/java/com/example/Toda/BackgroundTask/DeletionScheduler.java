package com.example.Toda.BackgroundTask;

import com.example.Toda.repo.UserRepo;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Configuration
public class DeletionScheduler {
    private final UserRepo userRepo;

    public DeletionScheduler(UserRepo userRepo) {
        this.userRepo = userRepo;
    }
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void permanentDeleteTask() {
        LocalDateTime now = LocalDateTime.now();
        userRepo.deleteByIsDeletedTrueAndDeletionDateBefore(now);
        System.out.println("Cleanup task: Permanent deletion completed at " + now);
    }

}
