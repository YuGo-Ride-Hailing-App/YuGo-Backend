package org.yugo.backend.YuGo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.yugo.backend.YuGo.dto.RideIn;
import org.yugo.backend.YuGo.model.Ride;

import java.time.LocalDateTime;
import java.util.List;

public interface RideService {
    Ride createRide(RideIn rideIn) throws Exception;
    Ride getUnresolvedRide(Integer userID);
    void notifyPassengersThatVehicleHasArrived(Integer rideID);
    Page<Ride> getRidesByDriverPage(Integer driverId, Pageable page, LocalDateTime start, LocalDateTime end);
    List<Ride> getAllByDate(LocalDateTime from, LocalDateTime to);
    Ride get(Integer id);
    void searchForDriver(Integer id);
    List<Ride> getAll();
    Ride getActiveRideByDriver(Integer id);
    Ride getActiveRideByPassenger(Integer id);
    Page<Ride> getPassengerRides(Integer passengerID, LocalDateTime from, LocalDateTime to, Pageable page);
    List<Ride> getPassengerRidesNonPageable(Integer passengerId, LocalDateTime from, LocalDateTime to);
    Page<Ride> getUserRides(Integer userID, LocalDateTime from, LocalDateTime to, Pageable page);
    List<Ride> getRidesByDriverNonPageable(Integer driverId, LocalDateTime start, LocalDateTime end);
    Ride cancelRide(Integer id);
    Ride startRide(Integer id);
    Ride acceptRide(Integer id);
    Ride endRide(Integer id);
    Ride rejectRide(Integer id,String reason);
    Ride save(Ride ride);
}
