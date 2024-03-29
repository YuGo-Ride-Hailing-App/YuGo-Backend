package org.yugo.backend.YuGo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.yugo.backend.YuGo.model.Ride;
import org.yugo.backend.YuGo.repository.RideRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;


@Component
public class ScheduledTasks {

    private final RideRepository rideRepository;
    private final RideService rideService;

    @Autowired
    public ScheduledTasks(RideRepository rideRepository, RideService rideService){
        this.rideRepository = rideRepository;
        this.rideService = rideService;
    }

    @Scheduled(fixedRate = 60000)
    public void scheduleRides() {
        List<Ride> ridesToSchedule = rideRepository.findScheduledRides();
        for(Ride ride : ridesToSchedule){
            if (ChronoUnit.MINUTES.between(LocalDateTime.now(), ride.getStartTime())<31)
                rideService.searchForDriver(ride.getId());
        }
    }
}
