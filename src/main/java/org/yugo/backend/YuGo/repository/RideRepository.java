package org.yugo.backend.YuGo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.yugo.backend.YuGo.model.Passenger;
import org.yugo.backend.YuGo.model.Ride;
import org.yugo.backend.YuGo.model.User;
import org.yugo.backend.YuGo.model.WorkTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride,Integer> {

    //@Query(value = "SELECT r FROM Ride r WHERE r.driver.id=:driver_id AND r.startTime=(SELECT MIN(r2.startTime) FROM Ride r2 WHERE r2.startTime>CURRENT_TIMESTAMP AND r2.startTime='ACCEPTED')")
    @Query(value = "SELECT * FROM RIDES WHERE driver_id = :driver_id AND start_time = (SELECT MIN(start_time) FROM RIDES WHERE start_time > CURRENT_TIMESTAMP AND status = 'ACEPTED')",
            nativeQuery = true)
    Optional<Ride> getNextRide(@Param("driver_id") Integer driverID);
    @Query(value = "SELECT * FROM RIDES r WHERE r.driver_id = :driver_id and r.status='ACTIVE'",
            nativeQuery = true)
    Optional<Ride> findActiveRideByDriver(@Param("driver_id") Integer driverID);

    @Query(value = "SELECT * FROM RIDES r WHERE r.status='ACTIVE' and r.id in (SELECT ur.ride_id from PASSENGER_RIDES ur WHERE r.id=ur.ride_id and :passenger_id=ur.passenger_id)",
            nativeQuery = true)
    Optional<Ride> findActiveRideByPassenger(@Param("passenger_id") Integer passengerID);

    @Query(value = "SELECT * FROM rides WHERE :passenger_id in (SELECT passenger_id FROM PASSENGER_RIDES where  id=ride_id) AND  status IN ('PENDING', 'SCHEDULED')", nativeQuery = true)
    Optional<Ride> findUnresolvedRideByPassenger(@Param("passenger_id") Integer passengerID);

    @Query(value = "SELECT DISTINCT r FROM Ride r LEFT JOIN r.passengers p WHERE :passengerId = p.id AND r.startTime>=:fromTime AND :toTime>=r.startTime")
    Page<Ride> findRidesByPassenger(@Param("passengerId") Integer passengerId,
                                    @Param("fromTime") LocalDateTime fromTime,
                                    @Param("toTime") LocalDateTime toTime, Pageable page);
    @Query(value = "SELECT DISTINCT r FROM Ride r LEFT JOIN r.passengers p WHERE :passengerId = p.id AND r.startTime>=:fromTime AND :toTime>=r.startTime ORDER BY r.startTime DESC")
    List<Ride> findRidesByPassenger(@Param("passengerId") Integer passengerId,
                                    @Param("fromTime") LocalDateTime fromTime,
                                    @Param("toTime") LocalDateTime toTime);

    @Query(value = "SELECT DISTINCT r FROM Ride r LEFT JOIN r.passengers p WHERE (:userId = p.id OR :userId = r.driver.id) AND r.startTime>=:fromTime AND :toTime>=r.startTime")
    Page<Ride> findRidesByUser(@Param("userId") Integer userId,
                                    @Param("fromTime") LocalDateTime fromTime,
                                    @Param("toTime") LocalDateTime toTime, Pageable page);

    @Query(value = "SELECT ride FROM Ride ride WHERE ride.driver.id = ?1 and ride.startTime >= ?2 and ride.startTime <= ?3")
    Page<Ride> findRidesByDriverAndStartTimeAndEndTimePageable(Integer driverId, Pageable page, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT ride FROM Ride ride WHERE ride.driver.id = ?1 and ride.startTime >= ?2 and ride.startTime <= ?3 order by ride.startTime desc ")
    List<Ride> findRidesByDriverAndStartTimeAndEndTimePageable(Integer driverId, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT * FROM rides WHERE start_time>=:from and start_time<=:to",nativeQuery = true)
    List<Ride> findAllByDate(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = "SELECT r FROM Ride  r WHERE r.status='SCHEDULED'")
    List<Ride> findScheduledRides();
    @Query(value = "SELECT r FROM Ride r WHERE r.driver.id = :driver_id AND r.status='ACCEPTED'")
    Optional<Ride> findAcceptedRideByDriver(@Param("driver_id") Integer driverID);

    @Query(value = "SELECT * FROM rides WHERE status='ACTIVE' AND driver_id IN (SELECT id FROM users WHERE vehicle_id = :vehicleID)", nativeQuery = true)
    Optional<Ride> getStartedRideByVehicle(Integer vehicleID);

}