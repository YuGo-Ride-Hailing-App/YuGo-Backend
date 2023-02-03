package org.yugo.backend.YuGo.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.yugo.backend.YuGo.dto.LocationInOut;
import org.yugo.backend.YuGo.dto.PathInOut;
import org.yugo.backend.YuGo.dto.RideIn;
import org.yugo.backend.YuGo.dto.RouteProperties;
import org.springframework.data.domain.*;
import org.yugo.backend.YuGo.exception.NoContentException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.yugo.backend.YuGo.exception.BadRequestException;
import org.yugo.backend.YuGo.exception.NotFoundException;
import org.yugo.backend.YuGo.model.*;
import org.yugo.backend.YuGo.repository.DriverRepository;
import org.yugo.backend.YuGo.repository.RideRepository;
import org.yugo.backend.YuGo.repository.WorkTimeRepository;
import org.yugo.backend.YuGo.service.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RideServiceTest {
    @Mock
    private RideRepository rideRepository;
    @Mock
    private DriverRepository driverRepository;

    @Mock
    private WorkTimeRepository workTimeRepository;
    @Mock
    private RoutingService routingService;
    @Mock
    private DriverService driverService;
    @Mock
    private UserService userService;
    @Mock
    private PassengerService passengerService;
    @Mock
    private VehicleService vehicleService;
    @Mock
    private WebSocketService webSocketService;

    @Captor
    private ArgumentCaptor<Ride> rideArgumentCaptor;

    //@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    @InjectMocks
    private RideServiceImpl rideService;

    @Test
    @DisplayName("Should get ride positive")
    public void shouldFindRideByID() {
        LocalDateTime start = LocalDateTime.of(2022, 5, 5, 5, 5);
        LocalDateTime end = LocalDateTime.of(2022, 5, 5, 5, 15);

        Ride ride = new Ride(1, start, end, 15, null, null, null, 10, null, RideStatus.PENDING, null, false, false, false, null);
        Mockito.when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        Ride actualRide = rideService.get(1);

        Assertions.assertEquals(ride.getId(), actualRide.getId());
    }

    @Test
    public void userWithUnresolvedRideReturnsUnresolvedRide(){
        Integer userID = 1;
        Ride ride = new Ride(userID,null,null,15,null,null,null,10,null,RideStatus.PENDING,null,false,false,false,null);
        Mockito.when(rideRepository.findUnresolvedRideByPassenger(userID)).thenReturn(Optional.of(ride));

        Ride actualRide = rideService.getUnresolvedRide(userID);

        Assertions.assertEquals(actualRide.getId(), ride.getId());
    }

    @Test
    public void userWithNoUnresolvedRidesThrowsNotFoundException(){
        Integer userID = 1;
        Mockito.when(rideRepository.findUnresolvedRideByPassenger(userID)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> {
            rideService.getUnresolvedRide(userID);
        });
    }

    @Test
    public void unresolvedRideForNullUserThrowsNotFoundException(){
        Integer userID = null;
        Mockito.when(rideRepository.findUnresolvedRideByPassenger(userID)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> {
            rideService.getUnresolvedRide(userID);
        });
    }

    @Test
    @DisplayName("Ride booking for more than 30 minutes in advance returns ride with status SCHEDULED")
    public void rideBookingForRideInFutureReturnsRideWithScheduledStatus(){

        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext securityContext= Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        User user = new Passenger();
        Integer userID = 1;
        user.setId(userID);
        Mockito.when(auth.getPrincipal()).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(rideRepository.findUnresolvedRideByPassenger(anyInt())).thenReturn(Optional.empty());

    @Test
    @DisplayName("Should save ride")
    public void shouldSaveRide() {
        Ride unsavedRide = new Ride(null, null, null, 15, null, null, null, 10, null, RideStatus.PENDING, null, false, false, false, null);
        Ride savedRide = new Ride(1, null, null, 15, null, null, null, 10, null, RideStatus.PENDING, null, false, false, false, null);

        Mockito.when(rideRepository.save(unsavedRide)).thenReturn(savedRide);
        Ride actualRide = rideService.save(unsavedRide);

        Assertions.assertEquals(savedRide.getId(), actualRide.getId());
        verify(rideRepository, times(1)).save(rideArgumentCaptor.capture());
    }

    @Test
    @DisplayName("Should reject pending ride")
    public void ShouldRejectPendingRide() {
        RideService rideServiceSpy = Mockito.spy(rideService);
        Passenger firstPassenger = new Passenger();
        firstPassenger.setId(2);
        Ride ride = new Ride(1, null, null, 15, null, List.of(firstPassenger), null, 10, null, RideStatus.PENDING, null, false, false, false, null);
        Driver driver = new Driver();
        driver.setId(1);
        Mockito.doReturn(ride).when(rideServiceSpy).get(1);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(driver);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Ride updatedRide = rideServiceSpy.rejectRide(1, "Reason");
        Assertions.assertEquals(RideStatus.REJECTED, updatedRide.getStatus());
        Assertions.assertNotNull(updatedRide.getRejection());
        verify(rideServiceSpy, times(1)).get(1);
        verify(webSocketService, times(1)).notifyPassengerAboutRide(ride.getId(), 2);
    }

    @Test
    @DisplayName("Should throw not found for invalid ride ID for rejection")
    public void ShouldThrowNotFoundForInvalidRideIdForRejection() {
        RideService rideServiceSpy = Mockito.spy(rideService);
        Passenger firstPassenger = new Passenger();
        firstPassenger.setId(2);
        Driver driver = new Driver();
        driver.setId(1);
        Mockito.doThrow(NotFoundException.class).when(rideServiceSpy).get(2);
        Assertions.assertThrows(NotFoundException.class, () -> rideServiceSpy.rejectRide(2, "Reason"));
    }

    @Test
    @DisplayName("Should reject scheduled ride")
    public void ShouldRejectScheduledRide() {
        Passenger passenger = new Passenger();
        passenger.setId(2);
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.SCHEDULED, null, false, false, false, null);
        Driver driver = new Driver();
        driver.setId(1);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(driver);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(rideRepository.findById(1)).thenReturn(Optional.of(ride));

        Ride updatedRide = rideService.rejectRide(1, "Reason");
        Assertions.assertEquals(RideStatus.REJECTED, updatedRide.getStatus());
        Assertions.assertNotNull(updatedRide.getRejection());
        verify(rideRepository, times(1)).findById(1);
        verify(webSocketService, times(1)).notifyPassengerAboutRide(ride.getId(), 2);
    }

    @Test
    @DisplayName("Should reject pending ride")
    public void ShouldRejectPendingRideWithNullReason() {
        Passenger passenger = new Passenger();
        passenger.setId(2);
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.PENDING, null, false, false, false, null);
        Driver driver = new Driver();
        driver.setId(1);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(driver);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(rideRepository.findById(1)).thenReturn(Optional.of(ride));

        Ride updatedRide = rideService.rejectRide(1, null);
        Assertions.assertEquals(RideStatus.REJECTED, updatedRide.getStatus());
        Assertions.assertNull(updatedRide.getRejection().getReason());
        verify(rideRepository, times(1)).findById(1);
        verify(webSocketService, times(1)).notifyPassengerAboutRide(ride.getId(), 2);
    }

    @Test
    @DisplayName("Shouldn't reject ride with invalid status")
    public void ShouldNotRejectRideWithInvalidStatus() {
        Passenger passenger = new Passenger();
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.CANCELED, null, false, false, false, null);
        Driver driver = new Driver();
        driver.setId(1);
        Mockito.when(rideRepository.findById(1)).thenReturn(Optional.of(ride));

        Assertions.assertThrows(BadRequestException.class, () -> rideService.rejectRide(1, "Reason"));
        verify(rideRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should end ride")
    public void ShouldEndRide() {
        Passenger passenger = new Passenger();
        passenger.setId(2);
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.ACTIVE, null, false, false, false, null);
        Mockito.when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        Ride updatedRide = rideService.endRide(1);
        Assertions.assertEquals(RideStatus.FINISHED, updatedRide.getStatus());
        verify(rideRepository, times(1)).findById(1);
        verify(webSocketService, times(1)).notifyPassengerAboutRideEnd(2, ride.getId());
    }

    @Test
    @DisplayName("Should throw not found for invalid ride ID for ride end")
    public void ShouldThrowNotFoundForInvalidRideIdForRideEnd() {
        RideService rideServiceSpy = Mockito.spy(rideService);
        Passenger firstPassenger = new Passenger();
        firstPassenger.setId(2);
        Driver driver = new Driver();
        driver.setId(1);
        Mockito.doThrow(NotFoundException.class).when(rideServiceSpy).get(2);
        Assertions.assertThrows(NotFoundException.class, () -> rideServiceSpy.endRide(2));
    }

    @Test
    @DisplayName("Shouldn't end ride with invalid status")
    public void ShouldNotEndRideWithInvalidStatus() {
        Passenger passenger = new Passenger();
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.FINISHED, null, false, false, false, null);
        Mockito.when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        Assertions.assertThrows(BadRequestException.class, () -> rideService.endRide(1));
        verify(rideRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should accept pending ride")
    public void ShouldAcceptPendingRide() {
        Passenger passenger = new Passenger();
        passenger.setId(2);
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.PENDING, null, false, false, false, null);
        Mockito.when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        Ride updatedRide = rideService.acceptRide(1);
        Assertions.assertEquals(RideStatus.ACCEPTED, updatedRide.getStatus());
        verify(rideRepository, times(1)).findById(1);
        verify(webSocketService, times(1)).notifyPassengerAboutRide(ride.getId(), 2);
    }

    @Test
    @DisplayName("Should accept scheduled ride")
    public void ShouldAcceptScheduledRide() {
        Passenger passenger = new Passenger();
        passenger.setId(2);
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.SCHEDULED, null, false, false, false, null);
        Mockito.when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        Ride updatedRide = rideService.acceptRide(1);
        Assertions.assertEquals(RideStatus.ACCEPTED, updatedRide.getStatus());
        verify(rideRepository, times(1)).findById(1);
        verify(webSocketService, times(1)).notifyPassengerAboutRide(ride.getId(), 2);
    }

    @Test
    @DisplayName("Should throw not found for invalid ride ID for ride accept")
    public void ShouldThrowNotFoundForInvalidRideIdForRideAccept() {
        RideService rideServiceSpy = Mockito.spy(rideService);
        Passenger firstPassenger = new Passenger();
        firstPassenger.setId(2);
        Driver driver = new Driver();
        driver.setId(1);
        Mockito.doThrow(NotFoundException.class).when(rideServiceSpy).get(2);
        Assertions.assertThrows(NotFoundException.class, () -> rideServiceSpy.acceptRide(2));
    }

    @Test
    @DisplayName("Shouldn't accept ride with invalid status")
    public void ShouldNotAcceptRideWithInvalidStatus() {
        Passenger passenger = new Passenger();
        passenger.setId(2);
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.FINISHED, null, false, false, false, null);
        Mockito.when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        Assertions.assertThrows(BadRequestException.class, () -> rideService.acceptRide(1));
        verify(rideRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should start accepted ride")
    public void ShouldStartAcceptedRide() {
        Passenger passenger = new Passenger();
        passenger.setId(2);
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.ACCEPTED, null, false, false, false, null);
        Mockito.when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        Ride updatedRide = rideService.startRide(1);
        Assertions.assertEquals(RideStatus.ACTIVE, updatedRide.getStatus());
        verify(rideRepository, times(1)).findById(1);
        verify(webSocketService, times(1)).notifyPassengerAboutRideStart(2);
    }

    @Test
    @DisplayName("Should throw not found for invalid ride ID for ride start")
    public void ShouldThrowNotFoundForInvalidRideIdForRideStart() {
        RideService rideServiceSpy = Mockito.spy(rideService);
        Passenger firstPassenger = new Passenger();
        firstPassenger.setId(2);
        Driver driver = new Driver();
        driver.setId(1);
        Mockito.doThrow(NotFoundException.class).when(rideServiceSpy).get(2);
        Assertions.assertThrows(NotFoundException.class, () -> rideServiceSpy.startRide(2));
    }

    @Test
    @DisplayName("Shouldn't start ride with invalid status")
    public void ShouldNotStartRideWithInvalidStatus() {
        Passenger passenger = new Passenger();
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.PENDING, null, false, false, false, null);
        Mockito.when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        Assertions.assertThrows(BadRequestException.class, () -> rideService.startRide(1));
        verify(rideRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should cancel active ride")
    public void ShouldCancelActiveRide() {
        Passenger passenger = new Passenger();
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.ACTIVE, null, false, false, false, null);
        Mockito.when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        Ride updatedRide = rideService.cancelRide(1);
        Assertions.assertEquals(RideStatus.CANCELED, updatedRide.getStatus());
        verify(rideRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should cancel pending ride")
    public void ShouldCancelPendingRide() {
        Passenger passenger = new Passenger();
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.PENDING, null, false, false, false, null);
        Mockito.when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        Ride updatedRide = rideService.cancelRide(1);
        Assertions.assertEquals(RideStatus.CANCELED, updatedRide.getStatus());
        verify(rideRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should cancel scheduled ride")
    public void ShouldCancelScheduledRide() {
        Passenger passenger = new Passenger();
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.SCHEDULED, null, false, false, false, null);
        Mockito.when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        Ride updatedRide = rideService.cancelRide(1);
        Assertions.assertEquals(RideStatus.CANCELED, updatedRide.getStatus());
        verify(rideRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should throw not found for invalid ride ID for ride cancel")
    public void ShouldThrowNotFoundForInvalidRideIdForRideCancel() {
        RideService rideServiceSpy = Mockito.spy(rideService);
        Passenger firstPassenger = new Passenger();
        firstPassenger.setId(2);
        Driver driver = new Driver();
        driver.setId(1);
        Mockito.doThrow(NotFoundException.class).when(rideServiceSpy).get(2);
        Assertions.assertThrows(NotFoundException.class, () -> rideServiceSpy.cancelRide(2));
    }

    @Test
    @DisplayName("Shouldn't cancel ride with invalid status")
    public void ShouldNotCancelRideWithInvalidStatus() {
        Passenger passenger = new Passenger();
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.FINISHED, null, false, false, false, null);
        Mockito.when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        Assertions.assertThrows(BadRequestException.class, () -> rideService.cancelRide(1));
        verify(rideRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should notify passengers that vehicle has arrived")
    public void ShouldNotifyPassengersThatVehicleHasArrived() {
        Passenger passenger = new Passenger();
        passenger.setId(2);
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.FINISHED, null, false, false, false, null);
        Mockito.when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        rideService.notifyPassengersThatVehicleHasArrived(1);
        verify(webSocketService, times(1)).notifyPassengerThatVehicleHasArrived(2, 1);
    }

    @Test
    @DisplayName("Should throw not found for invalid ride ID instead of notifying passengers")
    public void ShouldNotNotifyPassengersThatVehicleHasArrived() {
        RideService rideServiceSpy = Mockito.spy(rideService);
        Passenger passenger = new Passenger();
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.FINISHED, null, false, false, false, null);
        Mockito.doThrow(NotFoundException.class).when(rideServiceSpy).get(2);
        Assertions.assertThrows(NotFoundException.class, () -> rideServiceSpy.notifyPassengersThatVehicleHasArrived(2));
    }
}
