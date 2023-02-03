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
import org.yugo.backend.YuGo.exception.BadRequestException;
import org.yugo.backend.YuGo.exception.NotFoundException;
import org.yugo.backend.YuGo.model.*;
import org.yugo.backend.YuGo.repository.RideRepository;
import org.yugo.backend.YuGo.repository.WorkTimeRepository;
import org.yugo.backend.YuGo.service.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class RideServiceTest {
    @Mock
    private RideRepository rideRepository;

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
    public void shouldFindRideByID(){
        LocalDateTime start= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime end= LocalDateTime.of(2022,5,5,5,15);

        Ride ride = new Ride(1,start,end,15,null,null,null,10,null,RideStatus.PENDING,null,false,false,false,null);
        Mockito.when(rideRepository.findById(1)).thenReturn(Optional.of(ride));

        Ride actualRide= rideService.get(1);

        Assertions.assertEquals(actualRide.getId(),ride.getId());
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


        LocalDateTime now = LocalDateTime.now();
        LocalDateTime rideTime = now.plusMinutes(32);
        String rideTimeText = rideTime.format(DateTimeFormatter.ISO_DATE_TIME);

        List<PathInOut> locations = new ArrayList<>();
        PathInOut path = new PathInOut();
        LocationInOut departure = new LocationInOut();
        LocationInOut destination = new LocationInOut();
        path.setDeparture(departure);
        path.setDestination(destination);
        locations.add(path);

        double distance = 1000;
        double pricePerKM = 10;
        int duration = 300;
        VehicleType vehicleType = VehicleType.STANDARD;

        RouteProperties routeProperties = new RouteProperties();
        routeProperties.setDistance(distance);
        routeProperties.setDuration(duration);
        Mockito.when(routingService.getRouteProperties(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(routeProperties);

        VehicleTypePrice vehicleTypePrice = new VehicleTypePrice();
        vehicleTypePrice.setVehicleType(vehicleType);
        vehicleTypePrice.setPricePerKM(pricePerKM);
        vehicleTypePrice.setId(null);
        vehicleTypePrice.setImagePath(null);
        Mockito.when(vehicleService.getVehicleTypeByName(anyString())).thenReturn(vehicleTypePrice);

        Mockito.when(rideRepository.save(any(Ride.class))).thenAnswer(obj -> obj.getArguments()[0]);

        RideIn rideIn = new RideIn(locations,new ArrayList<>(),"STANDARD", false, false, rideTimeText);

        Ride rideExpected = rideService.createRide(rideIn);
        Assertions.assertEquals(RideStatus.SCHEDULED, rideExpected.getStatus());
    }

    @Test
    @DisplayName("Ride booking for less than 30 minutes in advance returns ride with status PENDING")
    public void rideBookingForRideInNearFutureReturnsRideWithPendingStatus(){

        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext securityContext= Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        User user = new Passenger();
        Integer userID = 1;
        user.setId(userID);
        Mockito.when(auth.getPrincipal()).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(rideRepository.findUnresolvedRideByPassenger(anyInt())).thenReturn(Optional.empty());


        LocalDateTime now = LocalDateTime.now();
        LocalDateTime rideTime = now.plusMinutes(28);
        String rideTimeText = rideTime.format(DateTimeFormatter.ISO_DATE_TIME);

        List<PathInOut> locations = new ArrayList<>();
        PathInOut path = new PathInOut();
        LocationInOut departure = new LocationInOut();
        LocationInOut destination = new LocationInOut();
        path.setDeparture(departure);
        path.setDestination(destination);
        locations.add(path);

        double distance = 1000;
        double pricePerKM = 10;
        int duration = 300;
        VehicleType vehicleType = VehicleType.STANDARD;

        RouteProperties routeProperties = new RouteProperties();
        routeProperties.setDistance(distance);
        routeProperties.setDuration(duration);
        Mockito.when(routingService.getRouteProperties(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(routeProperties);

        VehicleTypePrice vehicleTypePrice = new VehicleTypePrice();
        vehicleTypePrice.setVehicleType(vehicleType);
        vehicleTypePrice.setPricePerKM(pricePerKM);
        vehicleTypePrice.setId(null);
        vehicleTypePrice.setImagePath(null);
        Mockito.when(vehicleService.getVehicleTypeByName(anyString())).thenReturn(vehicleTypePrice);

        Mockito.when(rideRepository.save(any(Ride.class))).thenAnswer(obj -> obj.getArguments()[0]);

        RideIn rideIn = new RideIn(locations,new ArrayList<>(),"STANDARD", false, false, rideTimeText);

        Ride rideExpected = rideService.createRide(rideIn);
        Assertions.assertEquals(RideStatus.PENDING, rideExpected.getStatus());
    }

    @Test
    public void rideBookingWithPassengerWithUnresolvedRideThrowsException(){

        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext securityContext= Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        User user = new Passenger();
        Integer userID = 1;
        user.setId(userID);
        Mockito.when(auth.getPrincipal()).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(rideRepository.findUnresolvedRideByPassenger(anyInt())).thenReturn(Optional.of(new Ride()));

        RideIn rideIn = new RideIn(null,new ArrayList<>(),"STANDARD", false, false, null);

        Assertions.assertThrows(BadRequestException.class, () -> {
                rideService.createRide(rideIn);
        });
    }

    @Test
    public void rideBookingWithBadVehicleTypeThrowsName(){

        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext securityContext= Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        User user = new Passenger();
        Integer userID = 1;
        user.setId(userID);
        Mockito.when(auth.getPrincipal()).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(rideRepository.findUnresolvedRideByPassenger(anyInt())).thenReturn(Optional.empty());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime rideTime = now.plusMinutes(28);
        String rideTimeText = rideTime.format(DateTimeFormatter.ISO_DATE_TIME);

        RideIn rideIn = new RideIn(null,new ArrayList<>(),"__()*)()", false, false, rideTimeText);

        Assertions.assertThrows(BadRequestException.class, () -> {
            rideService.createRide(rideIn);
        });
    }

    @Test
    public void notifyPassengersAboutVehicleArrivalNotifiesAllPassengersOfThatRide(){
        Integer rideID = 1;
        Ride ride = new Ride();
        Passenger passenger01 = new Passenger();
        passenger01.setId(1);
        Passenger passenger02 = new Passenger();
        passenger02.setId(2);
        List<Passenger> passengers = List.of(passenger01, passenger02);
        ride.setPassengers(passengers);
        RideService rideServiceSpy = Mockito.spy(rideService);
        Mockito.doReturn(ride).when(rideServiceSpy).get(rideID);

        rideServiceSpy.notifyPassengersThatVehicleHasArrived(rideID);
        for(Passenger passenger : passengers){
            Mockito.verify(webSocketService).notifyPassengerThatVehicleHasArrived(passenger.getId(), rideID);
        }
    }

    @Test
    @DisplayName("Should save ride")
    public void shouldSaveRide() {
        Ride unsavedRide = new Ride(null, null, null, 15, null, null, null, 10, null, RideStatus.PENDING, null, false, false, false, null);
        Ride savedRide = new Ride(1, null, null, 15, null, null, null, 10, null, RideStatus.PENDING, null, false, false, false, null);

        Mockito.when(rideRepository.save(unsavedRide)).thenReturn(savedRide);
        Ride actualRide = rideService.save(unsavedRide);

        Assertions.assertEquals(savedRide.getId(), actualRide.getId());
        Mockito.verify(rideRepository, Mockito.times(1)).save(rideArgumentCaptor.capture());
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
        Mockito.verify(rideServiceSpy, Mockito.times(1)).get(1);
        Mockito.verify(webSocketService, Mockito.times(1)).notifyPassengerAboutRide(ride.getId(), 2);
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
        RideService rideServiceSpy = Mockito.spy(rideService);
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
        Mockito.doReturn(ride).when(rideServiceSpy).get(1);

        Ride updatedRide = rideServiceSpy.rejectRide(1, "Reason");
        Assertions.assertEquals(RideStatus.REJECTED, updatedRide.getStatus());
        Assertions.assertNotNull(updatedRide.getRejection());
        Mockito.verify(rideServiceSpy, Mockito.times(1)).get(1);
        Mockito.verify(webSocketService, Mockito.times(1)).notifyPassengerAboutRide(ride.getId(), 2);
    }

    @Test
    @DisplayName("Should reject pending ride")
    public void ShouldRejectPendingRideWithNullReason() {
        RideService rideServiceSpy = Mockito.spy(rideService);
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
        Mockito.doReturn(ride).when(rideServiceSpy).get(1);

        Ride updatedRide = rideServiceSpy.rejectRide(1, null);
        Assertions.assertEquals(RideStatus.REJECTED, updatedRide.getStatus());
        Assertions.assertNull(updatedRide.getRejection().getReason());
        Mockito.verify(rideServiceSpy, Mockito.times(1)).get(1);
        Mockito.verify(webSocketService, Mockito.times(1)).notifyPassengerAboutRide(ride.getId(), 2);
    }

    @Test
    @DisplayName("Shouldn't reject ride with invalid status")
    public void ShouldNotRejectRideWithInvalidStatus() {
        RideService rideServiceSpy = Mockito.spy(rideService);
        Passenger passenger = new Passenger();
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.CANCELED, null, false, false, false, null);
        Driver driver = new Driver();
        driver.setId(1);
        Mockito.doReturn(ride).when(rideServiceSpy).get(1);

        Assertions.assertThrows(BadRequestException.class, () -> rideServiceSpy.rejectRide(1, "Reason"));
        Mockito.verify(rideServiceSpy, Mockito.times(1)).get(1);
    }

    @Test
    @DisplayName("Should end ride")
    public void ShouldEndRide() {
        RideService rideServiceSpy = Mockito.spy(rideService);
        Passenger passenger = new Passenger();
        passenger.setId(2);
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.ACTIVE, null, false, false, false, null);
        Mockito.doReturn(ride).when(rideServiceSpy).get(1);
        Ride updatedRide = rideServiceSpy.endRide(1);
        Assertions.assertEquals(RideStatus.FINISHED, updatedRide.getStatus());
        Mockito.verify(rideServiceSpy, Mockito.times(1)).get(1);
        Mockito.verify(webSocketService, Mockito.times(1)).notifyPassengerAboutRideEnd(2, ride.getId());
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
        RideService rideServiceSpy = Mockito.spy(rideService);
        Passenger passenger = new Passenger();
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.FINISHED, null, false, false, false, null);
        Mockito.doReturn(ride).when(rideServiceSpy).get(1);
        Assertions.assertThrows(BadRequestException.class, () -> rideServiceSpy.endRide(1));
        Mockito.verify(rideServiceSpy, Mockito.times(1)).get(1);
    }

    @Test
    @DisplayName("Should accept pending ride")
    public void ShouldAcceptPendingRide() {
        RideService rideServiceSpy = Mockito.spy(rideService);
        Passenger passenger = new Passenger();
        passenger.setId(2);
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.PENDING, null, false, false, false, null);
        Mockito.doReturn(ride).when(rideServiceSpy).get(1);
        Ride updatedRide = rideServiceSpy.acceptRide(1);
        Assertions.assertEquals(RideStatus.ACCEPTED, updatedRide.getStatus());
        Mockito.verify(rideServiceSpy, Mockito.times(1)).get(1);
        Mockito.verify(webSocketService, Mockito.times(1)).notifyPassengerAboutRide(ride.getId(), 2);
    }

    @Test
    @DisplayName("Should accept scheduled ride")
    public void ShouldAcceptScheduledRide() {
        RideService rideServiceSpy = Mockito.spy(rideService);
        Passenger passenger = new Passenger();
        passenger.setId(2);
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.SCHEDULED, null, false, false, false, null);
        Mockito.doReturn(ride).when(rideServiceSpy).get(1);
        Ride updatedRide = rideServiceSpy.acceptRide(1);
        Assertions.assertEquals(RideStatus.ACCEPTED, updatedRide.getStatus());
        Mockito.verify(rideServiceSpy, Mockito.times(1)).get(1);
        Mockito.verify(webSocketService, Mockito.times(1)).notifyPassengerAboutRide(ride.getId(), 2);
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
        RideService rideServiceSpy = Mockito.spy(rideService);
        Passenger passenger = new Passenger();
        passenger.setId(2);
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.FINISHED, null, false, false, false, null);
        Mockito.doReturn(ride).when(rideServiceSpy).get(1);
        Assertions.assertThrows(BadRequestException.class, () -> rideServiceSpy.acceptRide(1));
        Mockito.verify(rideServiceSpy, Mockito.times(1)).get(1);
    }

    @Test
    @DisplayName("Should start accepted ride")
    public void ShouldStartAcceptedRide() {
        RideService rideServiceSpy = Mockito.spy(rideService);
        Passenger passenger = new Passenger();
        passenger.setId(2);
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.ACCEPTED, null, false, false, false, null);
        Mockito.doReturn(ride).when(rideServiceSpy).get(1);
        Ride updatedRide = rideServiceSpy.startRide(1);
        Assertions.assertEquals(RideStatus.ACTIVE, updatedRide.getStatus());
        Mockito.verify(rideServiceSpy, Mockito.times(1)).get(1);
        Mockito.verify(webSocketService, Mockito.times(1)).notifyPassengerAboutRideStart(2);
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
        RideService rideServiceSpy = Mockito.spy(rideService);
        Passenger passenger = new Passenger();
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.PENDING, null, false, false, false, null);
        Mockito.doReturn(ride).when(rideServiceSpy).get(1);
        Assertions.assertThrows(BadRequestException.class, () -> rideServiceSpy.startRide(1));
        Mockito.verify(rideServiceSpy, Mockito.times(1)).get(1);
    }

    @Test
    @DisplayName("Should cancel active ride")
    public void ShouldCancelActiveRide() {
        RideService rideServiceSpy = Mockito.spy(rideService);
        Passenger passenger = new Passenger();
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.ACTIVE, null, false, false, false, null);
        Mockito.doReturn(ride).when(rideServiceSpy).get(1);
        Ride updatedRide = rideServiceSpy.cancelRide(1);
        Assertions.assertEquals(RideStatus.CANCELED, updatedRide.getStatus());
        Mockito.verify(rideServiceSpy, Mockito.times(1)).get(1);
    }

    @Test
    @DisplayName("Should cancel pending ride")
    public void ShouldCancelPendingRide() {
        RideService rideServiceSpy = Mockito.spy(rideService);
        Passenger passenger = new Passenger();
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.PENDING, null, false, false, false, null);
        Mockito.doReturn(ride).when(rideServiceSpy).get(1);
        Ride updatedRide = rideServiceSpy.cancelRide(1);
        Assertions.assertEquals(RideStatus.CANCELED, updatedRide.getStatus());
        Mockito.verify(rideServiceSpy, Mockito.times(1)).get(1);
    }

    @Test
    @DisplayName("Should cancel scheduled ride")
    public void ShouldCancelScheduledRide() {
        RideService rideServiceSpy = Mockito.spy(rideService);
        Passenger passenger = new Passenger();
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.SCHEDULED, null, false, false, false, null);
        Mockito.doReturn(ride).when(rideServiceSpy).get(1);
        Ride updatedRide = rideServiceSpy.cancelRide(1);
        Assertions.assertEquals(RideStatus.CANCELED, updatedRide.getStatus());
        Mockito.verify(rideServiceSpy, Mockito.times(1)).get(1);
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
        RideService rideServiceSpy = Mockito.spy(rideService);
        Passenger passenger = new Passenger();
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.FINISHED, null, false, false, false, null);
        Mockito.doReturn(ride).when(rideServiceSpy).get(1);
        Assertions.assertThrows(BadRequestException.class, () -> rideServiceSpy.cancelRide(1));
        Mockito.verify(rideServiceSpy, Mockito.times(1)).get(1);
    }

    @Test
    @DisplayName("Should notify passengers that vehicle has arrived")
    public void ShouldNotifyPassengersThatVehicleHasArrived() {
        RideService rideServiceSpy = Mockito.spy(rideService);
        Passenger passenger = new Passenger();
        passenger.setId(2);
        Ride ride = new Ride(1, null, null, 15, null, List.of(passenger), null, 10, null, RideStatus.FINISHED, null, false, false, false, null);
        Mockito.doReturn(ride).when(rideServiceSpy).get(1);
        rideServiceSpy.notifyPassengersThatVehicleHasArrived(1);
        Mockito.verify(webSocketService, Mockito.times(1)).notifyPassengerThatVehicleHasArrived(2, 1);
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
