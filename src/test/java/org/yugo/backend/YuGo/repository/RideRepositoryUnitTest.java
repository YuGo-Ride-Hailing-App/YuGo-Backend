package org.yugo.backend.YuGo.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.yugo.backend.YuGo.model.Ride;
import org.yugo.backend.YuGo.model.RideStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        locations = "classpath:application-repository-test.properties")
@ActiveProfiles("repository_test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RideRepositoryUnitTest {
    @Autowired
    private RideRepository rideRepository;

    @Test
    @Order(1)
    @DisplayName("Should find next ride for driver")
    public void shouldFindNextRideForDriver(){
        Optional<Ride> ride = rideRepository.getNextRide(8);
        assertThat(ride).isNotEmpty();
    }

    @Test
    @Order(2)
    @DisplayName("Should not find next ride for passenger")
    public void shouldNotFindNextRideForPassenger(){
        Optional<Ride> ride = rideRepository.getNextRide(1);
        assertThat(ride).isEmpty();
    }

    @Test
    @Order(3)
    @DisplayName("Should not find next ride for driver")
    public void shouldNotFindNextRideForDriver(){
        Optional<Ride> ride = rideRepository.getNextRide(9);
        assertThat(ride).isEmpty();
    }

    @Test
    @Order(4)
    @DisplayName("Should find active ride for driver for driver Id")
    public void shouldFindActiveRideForDriverForDriverID(){
        Optional<Ride> ride = rideRepository.findActiveRideByDriver(7);
        assertThat(ride).isNotEmpty();
    }

    @Test
    @Order(5)
    @DisplayName("Should not find active ride for driver for driver Id")
    public void shouldNotFindActiveRideForDriverForDriverID(){
        Optional<Ride> ride = rideRepository.findActiveRideByDriver(9);
        assertThat(ride).isEmpty();
    }

    @Test
    @Order(6)
    @DisplayName("Should not find active ride for driver for passenger Id")
    public void shouldNotFindActiveRideForDriverForPassengerID(){
        Optional<Ride> ride = rideRepository.findActiveRideByDriver(1);
        assertThat(ride).isEmpty();
    }

    @Test
    @Order(7)
    @DisplayName("Should find active ride for passenger for passenger Id")
    public void shouldFindActiveRideForPassengerForPassengerID(){
        Optional<Ride> ride = rideRepository.findActiveRideByPassenger(3);
        assertThat(ride).isNotEmpty();
    }

    @Test
    @Order(8)
    @DisplayName("Should not find active ride for passenger for passenger Id")
    public void shouldNotFindActiveRideForPassengerForPassengerID(){
        Optional<Ride> ride = rideRepository.findActiveRideByPassenger(5);
        assertThat(ride).isEmpty();
    }

    @Test
    @Order(9)
    @DisplayName("Should not find active ride for passenger for driver Id")
    public void shouldNotFindActiveRideForPassengerForDriverID(){
        Optional<Ride> ride = rideRepository.findActiveRideByPassenger(7);
        assertThat(ride).isEmpty();
    }

    @Test
    @Order(10)
    @DisplayName("Should find unresolved ride for passenger for passenger Id")
    public void shouldFindUnresolvedRideForPassengerForPassengerID(){
        Optional<Ride> ride = rideRepository.findActiveRideByPassenger(3);
        assertThat(ride).isNotEmpty();
    }

    @Test
    @Order(11)
    @DisplayName("Should not find unresolved ride for passenger for passenger Id")
    public void shouldNotFindUnresolvedRideForPassengerForPassengerID(){
        Optional<Ride> ride = rideRepository.findActiveRideByPassenger(1);
        assertThat(ride).isEmpty();
    }

    @Test
    @Order(12)
    @DisplayName("Should find unresolved ride for passenger for driver Id")
    public void shouldNotFindUnresolvedRideForPassengerForDriverID(){
        Optional<Ride> ride = rideRepository.findActiveRideByPassenger(7);
        assertThat(ride).isEmpty();
    }

    @Test
    @Order(100)
    @DisplayName("Should get started ride by vehicle")
    public void shouldGetStartedRideByVehicle() {
        Optional<Ride> test = rideRepository.getStartedRideByVehicle(1);
        assertThat(test).isNotEmpty();
    }
    @Test
    @Order(101)
    @DisplayName("Shouldn't get started ride by vehicle vehicle not existing")
    public void shouldntGetStartedRideByVehicleEmptyVehicle() {
        Optional<Ride> test = rideRepository.getStartedRideByVehicle(4);
        assertThat(test).isEmpty();
    }

    @Test
    @Order(102)
    @DisplayName("Shouldn't get started ride by vehicle ride is not active")
    public void shouldntGetStartedRideByVehicleEmptyStatus() {
        Optional<Ride> test = rideRepository.getStartedRideByVehicle(3);
        assertThat(test).isEmpty();
    }

    @Test
    @Order(103)
    @DisplayName("Should get accepted ride by driver")
    public void shouldGetAcceptedRideByDriver() {
        Optional<Ride> test = rideRepository.findAcceptedRideByDriver(8);
        assertThat(test).isNotEmpty();
    }
    @Test
    @Order(104)
    @DisplayName("Shouldnt get accepted ride by driver driver not existing")
    public void shouldntGetAcceptedRideByDriverDriverNotExisting() {
        Optional<Ride> test = rideRepository.findAcceptedRideByDriver(108);
        assertThat(test).isEmpty();
    }
    @Test
    @Order(105)
    @DisplayName("Shouldnt get accepted ride by driver driver has not existing driver passenger")
    public void shouldntGetAcceptedRideByDriverRideNotExistingPassenger() {
        Optional<Ride> test = rideRepository.findAcceptedRideByDriver(1);
        assertThat(test).isEmpty();
    }

    @Test
    @Order(106)
    @DisplayName("Shouldnt get accepted ride by driver driver has not existing ride")
    public void shouldntGetAcceptedRideByDriverRideNotExisting() {
        Optional<Ride> test = rideRepository.findAcceptedRideByDriver(9);
        assertThat(test).isEmpty();
    }

    @Test
    @Order(107)
    @DisplayName("Should get scheduled rides")
    public void shouldntGetScheduledRides() {
        List<Ride> test = rideRepository.findScheduledRides();
        Assertions.assertEquals(0,test.size());
    }

    @Test
    @Order(108)
    @DisplayName("Should get scheduled rides")
    public void shouldGetScheduledRides() {
        Ride ride = new Ride(101,null,null,15,null,null,null,10,null, RideStatus.SCHEDULED,null,false,false,false,null);
        rideRepository.save(ride);
        List<Ride> test = rideRepository.findScheduledRides();
        Assertions.assertEquals(1,test.size());
    }

    @Test
    @Order(109)
    @DisplayName("Should get rides by date empty")
    public void shouldFindAllRidesByDateEmpty() {
        List<Ride> test = rideRepository.findAllByDate(LocalDateTime.of(2001,12,7,0,0),LocalDateTime.of(2002,12,7,0,0));
        Assertions.assertEquals(0,test.size());
    }

    @Test
    @Order(110)
    @DisplayName("Should get rides by date")
    public void shouldFindAllRidesByDate() {
        List<Ride> test = rideRepository.findAllByDate(LocalDateTime.of(2021,12,7,0,0),LocalDateTime.of(2024,12,7,0,0));
        Assertions.assertNotEquals(0,test.size());
    }

    @Test
    @Order(111)
    @DisplayName("Should get rides by driver and date empty")
    public void shouldfindRidesByDriverAndStartTimeAndEndTimePageableEmpty() {
        List<Ride> test = rideRepository.findRidesByDriverAndStartTimeAndEndTimePageable(6,LocalDateTime.of(2001,12,7,0,0),LocalDateTime.of(2002,12,7,0,0));
        Assertions.assertEquals(0,test.size());
    }

    @Test
    @Order(112)
    @DisplayName("Should get rides by driver and date empty")
    public void shouldfindRidesByDriverAndStartTimeAndEndTimePageable() {
        List<Ride> test = rideRepository.findRidesByDriverAndStartTimeAndEndTimePageable(6,LocalDateTime.of(2021,12,7,0,0),LocalDateTime.of(2024,12,7,0,0));
        Assertions.assertNotEquals(0,test.size());
    }

    @Test
    @Order(113)
    @DisplayName("Should get rides by driver and date empty not existing driver")
    public void shouldfindRidesByDriverAndStartTimeAndEndTimePageableNotExistingDriver() {
        List<Ride> test = rideRepository.findRidesByDriverAndStartTimeAndEndTimePageable(600,LocalDateTime.of(2021,12,7,0,0),LocalDateTime.of(2024,12,7,0,0));
        Assertions.assertEquals(0,test.size());
    }

    @Test
    @Order(114)
    @DisplayName("Should get rides by driver and date empty not  driver")
    public void shouldfindRidesByDriverAndStartTimeAndEndTimePageableNotDriver() {
        List<Ride> test = rideRepository.findRidesByDriverAndStartTimeAndEndTimePageable(1,LocalDateTime.of(2021,12,7,0,0),LocalDateTime.of(2024,12,7,0,0));
        Assertions.assertEquals(0,test.size());
    }



}
