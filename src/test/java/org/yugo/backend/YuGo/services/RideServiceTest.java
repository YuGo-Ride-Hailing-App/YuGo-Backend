package org.yugo.backend.YuGo.services;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.yugo.backend.YuGo.dto.LocationInOut;
import org.yugo.backend.YuGo.dto.PathInOut;
import org.yugo.backend.YuGo.dto.RideIn;
import org.yugo.backend.YuGo.dto.RouteProperties;
import org.yugo.backend.YuGo.exception.BadRequestException;
import org.yugo.backend.YuGo.exception.NoContentException;
import org.yugo.backend.YuGo.exception.NotFoundException;
import org.yugo.backend.YuGo.model.*;
import org.yugo.backend.YuGo.repository.RideRepository;
import org.yugo.backend.YuGo.repository.WorkTimeRepository;
import org.yugo.backend.YuGo.service.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;

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
    public void driverSearchWithNoAvailableDriversNotifiesPassengers(){
        Integer rideID = 1;
        Ride ride = new Ride();
        ride.setPetTransport(true);
        ride.setBabyTransport(true);
        ride.setId(rideID);
        List<Path> paths = new ArrayList<>();
        Path path = new Path();
        path.setDeparture(new Location());
        path.setDestination(new Location());
        paths.add(path);
        ride.setLocations(paths);
        ride.setVehicleTypePrice(new VehicleTypePrice());
        Passenger p1 = new Passenger();
        p1.setId(1);
        Passenger p2 = new Passenger();
        p2.setId(2);
        List<Passenger> passengers = List.of(p1, p2);
        ride.setPassengers(passengers);

        Mockito.when(driverService.getDriversInRange(anyDouble(), anyDouble(), anyDouble())).thenReturn(new ArrayList<>());
        Mockito.when(rideRepository.getReferenceById(rideID)).thenReturn(ride);

        rideService.searchForDriver(rideID);

        for(Passenger passenger : passengers){
            Mockito.verify(webSocketService).notifyPassengerAboutRide(-1, passenger.getId());
        }
        Mockito.verify(rideRepository).delete(ride);

    }
    @Test
    public void driverSearchWithAvailableDrivers(){
        VehicleTypePrice vehicleTypePrice = new VehicleTypePrice();
        vehicleTypePrice.setVehicleType(VehicleType.STANDARD);


        Vehicle vehicle1 = new Vehicle();
        vehicle1.setVehicleType(VehicleType.STANDARD);
        vehicle1.setAreBabiesAllowed(true);
        vehicle1.setArePetsAllowed(true);
        vehicle1.setNumberOfSeats(4);
        Location vehicleLocation = new Location();
        vehicleLocation.setLatitude(44);
        vehicleLocation.setLongitude(44);
        vehicle1.setCurrentLocation(vehicleLocation);

        List<Driver> drivers = new ArrayList<>();
        Integer driver1ID = 1;
        Driver driver1 = new Driver();
        driver1.setId(driver1ID);
        driver1.setVehicle(vehicle1);
        driver1.setOnline(true);
        drivers.add(driver1);

        Mockito.when(workTimeRepository.getTotalWorkTimeInLast24Hours(driver1ID)).thenReturn(0.0);
        Mockito.when(driverService.getDriversInRange(anyDouble(), anyDouble(), anyDouble())).thenReturn(drivers);
        Mockito.when(rideRepository.findAcceptedRideByDriver(driver1ID)).thenReturn(Optional.empty());
        Mockito.when(rideRepository.findActiveRideByDriver(driver1ID)).thenReturn(Optional.empty());
        Mockito.when(rideRepository.getNextRide(driver1ID)).thenReturn(Optional.empty());

        Integer rideID = 10;
        Ride ride = new Ride();
        ride.setVehicleTypePrice(vehicleTypePrice);
        ride.setPetTransport(true);
        ride.setBabyTransport(true);
        ride.setId(rideID);
        ride.setStartTime(LocalDateTime.now());
        List<Path> paths = new ArrayList<>();
        Path path = new Path();
        Location departure = new Location();
        departure.setLatitude(46);
        departure.setLongitude(46);
        path.setDeparture(departure);
        Location destination = new Location();
        destination.setLatitude(46);
        destination.setLongitude(46);
        path.setDestination(destination);
        paths.add(path);
        ride.setLocations(paths);
        Passenger p1 = new Passenger();
        p1.setId(1);
        Passenger p2 = new Passenger();
        p2.setId(2);
        List<Passenger> passengers = List.of(p1, p2);
        ride.setPassengers(passengers);
        Mockito.when(rideRepository.save(any(Ride.class))).thenAnswer(obj -> obj.getArguments()[0]);
        Mockito.when(rideRepository.getReferenceById(rideID)).thenReturn(ride);


        RouteProperties routeProperties = new RouteProperties();
        routeProperties.setDuration(600);
        Mockito.when(routingService.getRouteProperties(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(routeProperties);

        rideService.searchForDriver(rideID);

        ArgumentCaptor<Ride> argumentCaptor = ArgumentCaptor.forClass(Ride.class);
        Mockito.verify(rideRepository).save(argumentCaptor.capture());
        Assertions.assertEquals(driver1ID, argumentCaptor.getValue().getDriver().getId());
        Mockito.verify(webSocketService).sendRideRequestToDriver(driver1ID,ride.getId());
    }

    @Test
    public void driverSearchWithAvailableDriversPickDriverWithEarliestAvailableTime(){
        VehicleTypePrice vehicleTypePrice = new VehicleTypePrice();
        vehicleTypePrice.setVehicleType(VehicleType.STANDARD);

        Location vehicleLocation1 = new Location();
        vehicleLocation1.setLatitude(50);
        vehicleLocation1.setLongitude(50);
        Location vehicleLocation2 = new Location();
        vehicleLocation2.setLatitude(40);
        vehicleLocation2.setLongitude(40);

        Vehicle vehicle1 = new Vehicle();
        vehicle1.setVehicleType(VehicleType.STANDARD);
        vehicle1.setAreBabiesAllowed(true);
        vehicle1.setArePetsAllowed(true);
        vehicle1.setNumberOfSeats(4);
        vehicle1.setCurrentLocation(vehicleLocation1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setVehicleType(VehicleType.STANDARD);
        vehicle2.setAreBabiesAllowed(true);
        vehicle2.setArePetsAllowed(true);
        vehicle2.setNumberOfSeats(4);
        vehicle2.setCurrentLocation(vehicleLocation2);

        List<Driver> drivers = new ArrayList<>();
        Integer driver1ID = 1;
        Driver driver1 = new Driver();
        driver1.setId(driver1ID);
        driver1.setVehicle(vehicle1);
        driver1.setOnline(true);
        drivers.add(driver1);
        Integer driver2ID = 2;
        Driver driver2 = new Driver();
        driver2.setId(driver2ID);
        driver2.setVehicle(vehicle2);
        driver2.setOnline(true);
        drivers.add(driver2);

        Mockito.when(workTimeRepository.getTotalWorkTimeInLast24Hours(anyInt())).thenReturn(0.0);
        Mockito.when(driverService.getDriversInRange(anyDouble(), anyDouble(), anyDouble())).thenReturn(drivers);
        Mockito.when(rideRepository.findAcceptedRideByDriver(driver1ID)).thenReturn(Optional.empty());
        Mockito.when(rideRepository.findActiveRideByDriver(driver1ID)).thenReturn(Optional.empty());
        Mockito.when(rideRepository.getNextRide(driver1ID)).thenReturn(Optional.empty());

        Integer rideID = 1;
        Ride ride = new Ride();
        ride.setVehicleTypePrice(vehicleTypePrice);
        ride.setPetTransport(true);
        ride.setBabyTransport(true);
        ride.setId(rideID);
        ride.setStartTime(LocalDateTime.now());
        List<Path> paths = new ArrayList<>();
        Path path = new Path();
        Location departure = new Location();
        departure.setLatitude(46);
        departure.setLongitude(46);
        path.setDeparture(departure);
        Location destination = new Location();
        destination.setLatitude(46);
        destination.setLongitude(46);
        path.setDestination(destination);
        paths.add(path);
        ride.setLocations(paths);
        Passenger p1 = new Passenger();
        p1.setId(1);
        Passenger p2 = new Passenger();
        p2.setId(2);
        List<Passenger> passengers = List.of(p1, p2);
        ride.setPassengers(passengers);
        Mockito.when(rideRepository.save(any(Ride.class))).thenAnswer(obj -> obj.getArguments()[0]);
        Mockito.when(rideRepository.getReferenceById(rideID)).thenReturn(ride);


        RouteProperties routeProperties1 = new RouteProperties();
        routeProperties1.setDuration(1600);
        RouteProperties routeProperties2 = new RouteProperties();
        routeProperties2.setDuration(600);
        Mockito.when(routingService.getRouteProperties(vehicleLocation1.getLatitude(), vehicleLocation1.getLongitude(), departure.getLatitude(), departure.getLongitude())).thenReturn(routeProperties1);
        Mockito.when(routingService.getRouteProperties(vehicleLocation2.getLatitude(), vehicleLocation2.getLongitude(), departure.getLatitude(), departure.getLongitude())).thenReturn(routeProperties2);

        rideService.searchForDriver(rideID);


        ArgumentCaptor<Ride> argumentCaptor = ArgumentCaptor.forClass(Ride.class);
        Mockito.verify(rideRepository).save(argumentCaptor.capture());
        Assertions.assertEquals(driver2ID, argumentCaptor.getValue().getDriver().getId());
        Mockito.verify(webSocketService).sendRideRequestToDriver(driver2ID,ride.getId());
    }
    @Test
    @DisplayName("Should save ride")
    public void shouldSaveRide() {
        Ride unsavedRide = new Ride(null, null, null, 15, null, null, null, 10, null, RideStatus.PENDING, null, false, false, false, null);
        Ride savedRide = new Ride(1, null, null, 15, null, null, null, 10, null, RideStatus.PENDING, null, false, false, false, null);

        Mockito.when(rideRepository.save(unsavedRide)).thenReturn(savedRide);
        Ride actualRide = rideService.save(unsavedRide);

        Assertions.assertEquals(savedRide.getId(), actualRide.getId());
        Mockito.verify(rideRepository, times(1)).save(rideArgumentCaptor.capture());
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
        Mockito.verify(rideServiceSpy, times(1)).get(1);
        Mockito.verify(webSocketService, times(1)).notifyPassengerAboutRide(ride.getId(), 2);
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
        Mockito.verify(rideServiceSpy, times(1)).get(1);
        Mockito.verify(webSocketService, times(1)).notifyPassengerAboutRide(ride.getId(), 2);
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
        Mockito.verify(rideServiceSpy, times(1)).get(1);
        Mockito.verify(webSocketService, times(1)).notifyPassengerAboutRide(ride.getId(), 2);
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
        Mockito.verify(rideServiceSpy, times(1)).get(1);
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
        Mockito.verify(rideServiceSpy, times(1)).get(1);
        Mockito.verify(webSocketService, times(1)).notifyPassengerAboutRideEnd(2, ride.getId());
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
        Mockito.verify(rideServiceSpy, times(1)).get(1);
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
        Mockito.verify(rideServiceSpy, times(1)).get(1);
        Mockito.verify(webSocketService, times(1)).notifyPassengerAboutRide(ride.getId(), 2);
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
        Mockito.verify(rideServiceSpy, times(1)).get(1);
        Mockito.verify(webSocketService, times(1)).notifyPassengerAboutRide(ride.getId(), 2);
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
        Mockito.verify(rideServiceSpy, times(1)).get(1);
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
        Mockito.verify(rideServiceSpy, times(1)).get(1);
        Mockito.verify(webSocketService, times(1)).notifyPassengerAboutRideStart(2);
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
        Mockito.verify(rideServiceSpy, times(1)).get(1);
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
        Mockito.verify(rideServiceSpy, times(1)).get(1);
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
        Mockito.verify(rideServiceSpy, times(1)).get(1);
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
        Mockito.verify(rideServiceSpy, times(1)).get(1);
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
        Mockito.verify(rideServiceSpy, times(1)).get(1);
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
        Mockito.verify(webSocketService, times(1)).notifyPassengerThatVehicleHasArrived(2, 1);
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
    @Test
    @DisplayName("Should get ride negative id not exist")
    public void shouldFindRideByIDNotFound(){
        Mockito.when(rideRepository.findById(2)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class,()->{rideService.get(2);},"Ride does not exist!");
        Mockito.verify(rideRepository, times(1)).findById(2);
    }

    @Test
    @DisplayName("Should get ride negative id null")
    public void shouldFindRideByIDNull(){
        Mockito.when(rideRepository.findById(null)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class,()->{rideService.get(null);},"Ride does not exist!");
        Mockito.verify(rideRepository, times(1)).findById(null);
    }

    @Test
    @DisplayName("Should get all rides positive")
    public void shouldGetAllRides(){
        LocalDateTime start= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime end= LocalDateTime.of(2022,5,5,5,15);

        Ride ride = new Ride(1,start,end,15,null,null,null,10,null,RideStatus.PENDING,null,false,false,false,null);
        List<Ride> expectedRides=new ArrayList<>();
        expectedRides.add(ride);
        Mockito.when(rideRepository.findAll()).thenReturn(expectedRides);

        List<Ride> actualRides= rideService.getAll();

        Assertions.assertEquals(expectedRides.size(),actualRides.size());
        Mockito.verify(rideRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get all rides positive empty")
    public void shouldGetAllRidesEmpty(){
        LocalDateTime start= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime end= LocalDateTime.of(2022,5,5,5,15);

        Mockito.when(rideRepository.findAll()).thenReturn(new ArrayList<>());

        List<Ride> actualRides= rideService.getAll();

        Assertions.assertEquals(0,actualRides.size());
        Mockito.verify(rideRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get all rides by date positive")
    public void shouldGetAllRidesByDate(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,4,5,5);
        LocalDateTime start= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime start2= LocalDateTime.of(2023,5,5,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,6,5,15);
        LocalDateTime end= LocalDateTime.of(2022,5,5,5,15);
        LocalDateTime end2= LocalDateTime.of(2023,5,5,5,15);


        Ride ride = new Ride(1,start,end,15,null,null,null,10,null,RideStatus.PENDING,null,false,false,false,null);
        Ride ride2 = new Ride(1,start2,end2,15,null,null,null,10,null,RideStatus.PENDING,null,false,false,false,null);
        List<Ride> expectedRides=new ArrayList<>();
        expectedRides.add(ride);
        Mockito.when(rideRepository.findAllByDate(startCriteria,endCriteria)).thenReturn(expectedRides);

        List<Ride> actualRides= rideService.getAllByDate(startCriteria,endCriteria);

        Assertions.assertEquals(expectedRides.size(),actualRides.size());
        Mockito.verify(rideRepository, times(1)).findAllByDate(startCriteria,endCriteria);
    }

    @Test
    @DisplayName("Should get all rides by date positive empty")
    public void shouldGetAllRidesByDateEmpty(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,5,5,15);

        Mockito.when(rideRepository.findAllByDate(startCriteria,endCriteria)).thenReturn(new ArrayList<>());

        List<Ride> actualRides= rideService.getAllByDate(startCriteria,endCriteria);

        Assertions.assertEquals(0,actualRides.size());
        Mockito.verify(rideRepository, times(1)).findAllByDate(startCriteria,endCriteria);
    }

    @Test
    @DisplayName("Should get active ride by driver positive")
    public void shouldFindDriverActiveRide(){
        LocalDateTime start= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime end= LocalDateTime.of(2022,5,5,5,15);
        Driver driver=new Driver();
        driver.setId(1);
        Ride ride = new Ride(1,start,end,15,driver,null,null,10,null,RideStatus.ACTIVE,null,false,false,false,null);
        Mockito.when(rideRepository.findActiveRideByDriver(1)).thenReturn(Optional.of(ride));
        Ride actualRide= rideService.getActiveRideByDriver(1);

        Assertions.assertEquals(actualRide.getId(),ride.getId());
        Assertions.assertEquals(start,ride.getStartTime());
        Assertions.assertEquals(end,ride.getEndTime());
        Assertions.assertEquals(RideStatus.ACTIVE,ride.getStatus());
        Assertions.assertEquals(driver.getId(),ride.getDriver().getId());
        Mockito.verify(rideRepository, times(1)).findActiveRideByDriver(1);
    }

    @Test
    @DisplayName("Should get active ride by non existing driver")
    public void shouldntFindDriverNonExisting(){
        LocalDateTime start= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime end= LocalDateTime.of(2022,5,5,5,15);
        Driver driver=new Driver();
        driver.setId(1);
        Ride ride = new Ride(1,start,end,15,driver,null,null,10,null,RideStatus.ACTIVE,null,false,false,false,null);

        Assertions.assertThrows(NoContentException.class,()->{rideService.getActiveRideByDriver(2);});
    }

    @Test
    @DisplayName("Shouldn't have active ride ")
    public void shouldntFindDriverActiveRide(){
        LocalDateTime start= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime end= LocalDateTime.of(2022,5,5,5,15);
        Driver driver=new Driver();
        driver.setId(1);
        Ride ride = new Ride(1,start,end,15,driver,null,null,10,null,RideStatus.PENDING,null,false,false,false,null);
        Mockito.when(rideRepository.findActiveRideByDriver(1)).thenReturn(Optional.empty());

        Assertions.assertThrows(NoContentException.class,()->{rideService.getActiveRideByDriver(1);});
        Mockito.verify(rideRepository, times(1)).findActiveRideByDriver(1);
    }

    @Test
    @DisplayName("Should get active ride by passenger positive")
    public void shouldFindPassengerActiveRide(){
        LocalDateTime start= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime end= LocalDateTime.of(2022,5,5,5,15);
        Passenger passenger=new Passenger();
        passenger.setId(1);
        ArrayList<Passenger> passengers=new ArrayList();
        passengers.add(passenger);
        Ride ride = new Ride(1,start,end,15,null,passengers,null,10,null,RideStatus.ACTIVE,null,false,false,false,null);
        Mockito.when(rideRepository.findActiveRideByPassenger(1)).thenReturn(Optional.of(ride));
        Ride actualRide= rideService.getActiveRideByPassenger(passenger.getId());

        Assertions.assertEquals(actualRide.getId(),ride.getId());
        Assertions.assertEquals(start,ride.getStartTime());
        Assertions.assertEquals(end,ride.getEndTime());
        Assertions.assertEquals(RideStatus.ACTIVE,ride.getStatus());
        Assertions.assertTrue(passengers.contains(passenger));
        Mockito.verify(rideRepository, times(1)).findActiveRideByPassenger(1);
    }

    @Test
    @DisplayName("Should get active ride by non existing driver")
    public void shouldntFindPassengerNonExisting(){
        LocalDateTime start= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime end= LocalDateTime.of(2022,5,5,5,15);;
        Passenger passenger=new Passenger();
        passenger.setId(1);
        ArrayList<Passenger> passengers=new ArrayList();
        passengers.add(passenger);
        Ride ride = new Ride(1,start,end,15, null,passengers,null,10,null,RideStatus.ACTIVE,null,false,false,false,null);

        Assertions.assertThrows(NotFoundException.class,()->{rideService.getActiveRideByPassenger(2);});
    }

    @Test
    @DisplayName("Shouldn't have active ride ")
    public void shouldntFindPassengerActiveRide(){
        LocalDateTime start= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime end= LocalDateTime.of(2022,5,5,5,15);
        Passenger passenger=new Passenger();
        passenger.setId(1);
        ArrayList<Passenger> passengers=new ArrayList();
        passengers.add(passenger);
        Ride ride = new Ride(1,start,end,15,null,passengers,null,10,null,RideStatus.PENDING,null,false,false,false,null);
        Mockito.when(rideRepository.findActiveRideByPassenger(1)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class,()->{rideService.getActiveRideByPassenger(1);});
        Mockito.verify(rideRepository, times(1)).findActiveRideByPassenger(1);
    }


    @Test
    @DisplayName("Should get all rides by passenger positive")
    public void shouldGetAllRidesByPassenger(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,4,5,5);
        LocalDateTime start= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime start2= LocalDateTime.of(2023,5,5,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,6,5,15);
        LocalDateTime end= LocalDateTime.of(2022,5,5,5,15);
        LocalDateTime end2= LocalDateTime.of(2023,5,5,5,15);
        Passenger passenger=new Passenger();
        passenger.setId(1);
        ArrayList<Passenger> passengers=new ArrayList();
        passengers.add(passenger);

        Ride ride = new Ride(1,start,end,15,null,passengers,null,10,null,RideStatus.PENDING,null,false,false,false,null);
        Ride ride2 = new Ride(1,start2,end2,15,null,null,null,10,null,RideStatus.PENDING,null,false,false,false,null);
        List<Ride> expectedRides=new ArrayList<>();
        expectedRides.add(ride);
        Page<Ride> page= new PageImpl<>(expectedRides);

        Mockito.when(rideRepository.findRidesByPassenger(1,startCriteria,endCriteria, PageRequest.of(0, 5))).thenReturn(page);

        Page<Ride> actualRides= rideService.getPassengerRides(1,startCriteria,endCriteria,PageRequest.of(0, 5));
        Assertions.assertEquals(page.getTotalElements(),actualRides.getTotalElements());
        Assertions.assertEquals(page.getTotalPages(),actualRides.getTotalPages());
        Mockito.verify(rideRepository, times(1)).findRidesByPassenger(1,startCriteria,endCriteria,PageRequest.of(0, 5));
    }

    @Test
    @DisplayName("Should get all rides by passenger non exist")
    public void shouldGetAllRidesByPassengerNonExisting(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,5,5,15);
        Mockito.when(passengerService.get(2)).thenThrow(NotFoundException.class);

        Assertions.assertThrows(NotFoundException.class,()->{rideService.getPassengerRides(2,startCriteria,endCriteria,PageRequest.of(0, 5));},"Passenger does not exist!");
        Mockito.verify(rideRepository, times(0)).findRidesByPassenger(2,startCriteria,endCriteria,PageRequest.of(0, 5));
    }

    @Test
    @DisplayName("Should get all rides by passenger without rides")
    public void shouldGetAllRidesByPassengerEmptyRides(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,4,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,6,5,15);
        Passenger passenger=new Passenger();
        passenger.setId(1);
        ArrayList<Passenger> passengers=new ArrayList();
        passengers.add(passenger);

        List<Ride> expectedRides=new ArrayList<>();
        Page<Ride> page= new PageImpl<>(expectedRides);

        Mockito.when(rideRepository.findRidesByPassenger(1,startCriteria,endCriteria,PageRequest.of(0, 5))).thenReturn(page);
        Page<Ride> actualRides= rideService.getPassengerRides(1,startCriteria,endCriteria,PageRequest.of(0, 5));
        Assertions.assertEquals(page.getTotalElements(),actualRides.getTotalElements());
        Assertions.assertEquals(page.getTotalPages(),actualRides.getTotalPages());
        Mockito.verify(rideRepository, times(1)).findRidesByPassenger(1,startCriteria,endCriteria,PageRequest.of(0, 5));
    }

    @Test
    @DisplayName("Should get all rides by passenger invalid Pageable object")
    public void shouldGetAllRidesByPassengerInvalidPageable(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,4,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,6,5,15);
        Passenger passenger=new Passenger();
        passenger.setId(1);
        ArrayList<Passenger> passengers=new ArrayList();
        passengers.add(passenger);

        Assertions.assertThrows(IllegalArgumentException.class,()->{rideService.getPassengerRides(2,startCriteria,endCriteria,PageRequest.of(-1, 5));},"Passenger does not exist!");

    }

    @Test
    @DisplayName("Should get all rides by passenger pageable positive")
    public void shouldGetAllRidesByPassengerNonPageable(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,4,5,5);
        LocalDateTime start= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime start2= LocalDateTime.of(2023,5,5,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,6,5,15);
        LocalDateTime end= LocalDateTime.of(2022,5,5,5,15);
        LocalDateTime end2= LocalDateTime.of(2023,5,5,5,15);
        Passenger passenger=new Passenger();
        passenger.setId(1);
        ArrayList<Passenger> passengers=new ArrayList();
        passengers.add(passenger);

        Ride ride = new Ride(1,start,end,15,null,null,null,10,null,RideStatus.PENDING,null,false,false,false,null);
        Ride ride2 = new Ride(1,start2,end2,15,null,null,null,10,null,RideStatus.PENDING,null,false,false,false,null);
        List<Ride> expectedRides=new ArrayList<>();
        expectedRides.add(ride);
        Mockito.when(rideRepository.findRidesByPassenger(1,startCriteria,endCriteria)).thenReturn(expectedRides);

        List<Ride> actualRides= rideService.getPassengerRidesNonPageable(1,startCriteria,endCriteria);

        Assertions.assertEquals(expectedRides.size(),actualRides.size());
        Mockito.verify(rideRepository, times(1)).findRidesByPassenger(1,startCriteria,endCriteria);
    }

    @Test
    @DisplayName("Should get all rides by passenger non pagable non exist")
    public void shouldGetAllRidesByPassengerNonExistingNonPagable(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,5,5,15);
        Mockito.when(passengerService.get(2)).thenThrow(NotFoundException.class);

        Assertions.assertThrows(NotFoundException.class,()->{rideService.getPassengerRidesNonPageable(2,startCriteria,endCriteria);},"Passenger does not exist!");
        Mockito.verify(rideRepository, times(0)).findRidesByPassenger(2,startCriteria,endCriteria);
    }

    @Test
    @DisplayName("Should get all rides by passenger non pageable without rides")
    public void shouldGetAllRidesByPassengerNonPagableEmptyRides(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,4,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,6,5,15);
        Passenger passenger=new Passenger();
        passenger.setId(1);
        ArrayList<Passenger> passengers=new ArrayList();
        passengers.add(passenger);

        List<Ride> expectedRides=new ArrayList<>();

        Mockito.when(rideRepository.findRidesByPassenger(1,startCriteria,endCriteria)).thenReturn(expectedRides);
        List<Ride> actualRides= rideService.getPassengerRidesNonPageable(1,startCriteria,endCriteria);
        Assertions.assertEquals(actualRides.size(),expectedRides.size());
        Mockito.verify(rideRepository, times(1)).findRidesByPassenger(1,startCriteria,endCriteria);
    }

    @Test
    @DisplayName("Should get all rides by user positive")
    public void shouldGetAllRidesByUser(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,4,5,5);
        LocalDateTime start= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime start2= LocalDateTime.of(2023,5,5,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,6,5,15);
        LocalDateTime end= LocalDateTime.of(2022,5,5,5,15);
        LocalDateTime end2= LocalDateTime.of(2023,5,5,5,15);
        Passenger passenger=new Passenger();
        passenger.setId(1);
        ArrayList<Passenger> passengers=new ArrayList();
        passengers.add(passenger);

        Ride ride = new Ride(1,start,end,15,null,passengers,null,10,null,RideStatus.PENDING,null,false,false,false,null);
        Ride ride2 = new Ride(1,start2,end2,15,null,null,null,10,null,RideStatus.PENDING,null,false,false,false,null);
        List<Ride> expectedRides=new ArrayList<>();
        expectedRides.add(ride);
        Page<Ride> page= new PageImpl<>(expectedRides);

        Mockito.when(rideRepository.findRidesByUser(1,startCriteria,endCriteria,PageRequest.of(0, 5))).thenReturn(page);

        Page<Ride> actualRides= rideService.getUserRides(1,startCriteria,endCriteria,PageRequest.of(0, 5));
        Assertions.assertEquals(page.getTotalElements(),actualRides.getTotalElements());
        Assertions.assertEquals(page.getTotalPages(),actualRides.getTotalPages());
        Mockito.verify(rideRepository, times(1)).findRidesByUser(1,startCriteria,endCriteria,PageRequest.of(0, 5));
    }

    @Test
    @DisplayName("Should get all rides by user non exist")
    public void shouldGetAllRidesByUserNonExisting(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,5,5,15);
        Mockito.when(userService.getUser(2)).thenThrow(NotFoundException.class);

        Assertions.assertThrows(NotFoundException.class,()->{rideService.getUserRides(2,startCriteria,endCriteria,PageRequest.of(0, 5));},"User does not exist!");
        Mockito.verify(rideRepository, times(0)).findRidesByUser(2,startCriteria,endCriteria,PageRequest.of(0, 5));
    }

    @Test
    @DisplayName("Should get all rides by user without rides")
    public void shouldGetAllRidesByUserEmptyRides(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,4,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,6,5,15);
        Passenger passenger=new Passenger();
        passenger.setId(1);
        ArrayList<Passenger> passengers=new ArrayList();
        passengers.add(passenger);

        List<Ride> expectedRides=new ArrayList<>();
        Page<Ride> page= new PageImpl<>(expectedRides);

        Mockito.when(rideRepository.findRidesByUser(1,startCriteria,endCriteria,PageRequest.of(0, 5))).thenReturn(page);
        Page<Ride> actualRides= rideService.getUserRides(1,startCriteria,endCriteria,PageRequest.of(0, 5));
        Assertions.assertEquals(page.getTotalElements(),actualRides.getTotalElements());
        Assertions.assertEquals(page.getTotalPages(),actualRides.getTotalPages());
        Mockito.verify(rideRepository, times(1)).findRidesByUser(1,startCriteria,endCriteria,PageRequest.of(0, 5));
    }

    @Test
    @DisplayName("Should get all rides by user invalid Pageable object")
    public void shouldGetAllRidesByPassengerInvalidPageablee(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,4,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,6,5,15);
        Passenger passenger=new Passenger();
        passenger.setId(1);
        ArrayList<Passenger> passengers=new ArrayList();
        passengers.add(passenger);

        Assertions.assertThrows(IllegalArgumentException.class,()->{rideService.getUserRides(2,startCriteria,endCriteria,PageRequest.of(-1, 5));},"Passenger does not exist!");

    }

    @Test
    @DisplayName("Should get ride positive")
    public void shouldFindUnresolvedRideByID(){
        LocalDateTime start= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime end= LocalDateTime.of(2022,5,5,5,15);

        Ride ride = new Ride(1,start,end,15,null,null,null,10,null,RideStatus.PENDING,null,false,false,false,null);
        Mockito.when(rideRepository.findUnresolvedRideByPassenger(1)).thenReturn(Optional.of(ride));

        Ride actualRide= rideService.getUnresolvedRide(1);

        Assertions.assertEquals(actualRide.getId(),ride.getId());
        Assertions.assertEquals(start,ride.getStartTime());
        Assertions.assertEquals(end,ride.getEndTime());
        Mockito.verify(rideRepository, times(1)).findUnresolvedRideByPassenger(1);
    }

    @Test
    @DisplayName("Should get unresolved ride negative id not exist")
    public void shouldFindUnresolvedRideByIDNotFound(){
        Mockito.when(rideRepository.findUnresolvedRideByPassenger(2)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class,()->{rideService.getUnresolvedRide(2);},"Ride does not exist!");
        Mockito.verify(rideRepository, times(1)).findUnresolvedRideByPassenger(2);
    }

    @Test
    @DisplayName("Should get unresolved ride negative id null")
    public void shouldFindUnresolvedRideByIDNull(){
        Mockito.when(rideRepository.findUnresolvedRideByPassenger(null)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class,()->{rideService.getUnresolvedRide(null);},"Ride does not exist!");
        Mockito.verify(rideRepository, times(1)).findUnresolvedRideByPassenger(null);
    }

    @Test
    @DisplayName("Should get all rides by driver positive")
    public void shouldGetAllRidesByDriver(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,4,5,5);
        LocalDateTime start= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime start2= LocalDateTime.of(2023,5,5,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,6,5,15);
        LocalDateTime end= LocalDateTime.of(2022,5,5,5,15);
        LocalDateTime end2= LocalDateTime.of(2023,5,5,5,15);
        Driver driver=new Driver();
        driver.setId(1);

        Ride ride = new Ride(1,start,end,15,driver,null,null,10,null,RideStatus.PENDING,null,false,false,false,null);
        Ride ride2 = new Ride(1,start2,end2,15,null,null,null,10,null,RideStatus.PENDING,null,false,false,false,null);
        List<Ride> expectedRides=new ArrayList<>();
        expectedRides.add(ride);
        Page<Ride> page= new PageImpl<>(expectedRides);

        Mockito.when(rideRepository.findRidesByDriverAndStartTimeAndEndTimePageable(1,PageRequest.of(0, 5),startCriteria,endCriteria)).thenReturn(page);

        Page<Ride> actualRides= rideService.getRidesByDriverPage(1,PageRequest.of(0, 5),startCriteria,endCriteria);
        Assertions.assertEquals(page.getTotalElements(),actualRides.getTotalElements());
        Assertions.assertEquals(page.getTotalPages(),actualRides.getTotalPages());
        Mockito.verify(rideRepository, times(1)).findRidesByDriverAndStartTimeAndEndTimePageable(1,PageRequest.of(0, 5),startCriteria,endCriteria);
    }

    @Test
    @DisplayName("Should get all rides by driver non exist")
    public void shouldGetAllRidesByDriverNonExisting(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,5,5,15);
        Mockito.when(driverService.getDriver(2)).thenThrow(NotFoundException.class);

        Assertions.assertThrows(NotFoundException.class,()->{rideService.getRidesByDriverPage(2,PageRequest.of(0, 5),startCriteria,endCriteria);},"Passenger does not exist!");
        Mockito.verify(rideRepository, times(0)).findRidesByDriverAndStartTimeAndEndTimePageable(2,PageRequest.of(0, 5),startCriteria,endCriteria);
    }

    @Test
    @DisplayName("Should get all rides by driver without rides")
    public void shouldGetAllRidesByDriverEmptyRides(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,4,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,6,5,15);
        Driver driver=new Driver();
        driver.setId(1);

        List<Ride> expectedRides=new ArrayList<>();
        Page<Ride> page= new PageImpl<>(expectedRides);

        Mockito.when(rideRepository.findRidesByDriverAndStartTimeAndEndTimePageable(1,PageRequest.of(0, 5),startCriteria,endCriteria)).thenReturn(page);
        Page<Ride> actualRides= rideService.getRidesByDriverPage(1,PageRequest.of(0, 5),startCriteria,endCriteria);
        Assertions.assertEquals(page.getTotalElements(),actualRides.getTotalElements());
        Assertions.assertEquals(page.getTotalPages(),actualRides.getTotalPages());
        Mockito.verify(rideRepository, times(1)).findRidesByDriverAndStartTimeAndEndTimePageable(1,PageRequest.of(0, 5),startCriteria,endCriteria);
    }

    @Test
    @DisplayName("Should get all rides by driver invalid Pageable object")
    public void shouldGetAllRidesByDriverInvalidPageable(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,4,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,6,5,15);
        Driver driver=new Driver();
        driver.setId(1);

        Assertions.assertThrows(IllegalArgumentException.class,()->{rideService.getRidesByDriverPage(2,PageRequest.of(-1, 5),startCriteria,endCriteria);},"Passenger does not exist!");

    }

    @Test
    @DisplayName("Should get all rides by driver pageable positive")
    public void shouldGetAllRidesByDriverNonPageable(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,4,5,5);
        LocalDateTime start= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime start2= LocalDateTime.of(2023,5,5,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,6,5,15);
        LocalDateTime end= LocalDateTime.of(2022,5,5,5,15);
        LocalDateTime end2= LocalDateTime.of(2023,5,5,5,15);
        Driver driver=new Driver();
        driver.setId(1);

        Ride ride = new Ride(1,start,end,15,driver,null,null,10,null,RideStatus.PENDING,null,false,false,false,null);
        Ride ride2 = new Ride(1,start2,end2,15,null,null,null,10,null,RideStatus.PENDING,null,false,false,false,null);
        List<Ride> expectedRides=new ArrayList<>();
        expectedRides.add(ride);
        Mockito.when(rideRepository.findRidesByDriverAndStartTimeAndEndTimePageable(1,startCriteria,endCriteria)).thenReturn(expectedRides);

        List<Ride> actualRides= rideService.getRidesByDriverNonPageable(1,startCriteria,endCriteria);

        Assertions.assertEquals(expectedRides.size(),actualRides.size());
        Mockito.verify(rideRepository, times(1)).findRidesByDriverAndStartTimeAndEndTimePageable(1,startCriteria,endCriteria);
    }

    @Test
    @DisplayName("Should get all rides by driver non pagable non exist")
    public void shouldGetAllRidesByDriverNonExistingNonPagable(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,5,5,15);
        Mockito.when(driverService.getDriver(2)).thenThrow(NotFoundException.class);

        Assertions.assertThrows(NotFoundException.class,()->{rideService.getRidesByDriverNonPageable(2,startCriteria,endCriteria);},"Passenger does not exist!");
        Mockito.verify(rideRepository, times(0)).findRidesByDriverAndStartTimeAndEndTimePageable(2,startCriteria,endCriteria);
    }

    @Test
    @DisplayName("Should get all rides by driver non pageable without rides")
    public void shouldGetAllRidesByDriverNonPagableEmptyRides(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,4,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,6,5,15);
        Driver driver=new Driver();
        driver.setId(1);

        List<Ride> expectedRides=new ArrayList<>();

        Mockito.when(rideRepository.findRidesByDriverAndStartTimeAndEndTimePageable(1,startCriteria,endCriteria)).thenReturn(expectedRides);
        List<Ride> actualRides= rideService.getRidesByDriverNonPageable(1,startCriteria,endCriteria);
        Assertions.assertEquals(actualRides.size(),expectedRides.size());
        Mockito.verify(rideRepository, times(1)).findRidesByDriverAndStartTimeAndEndTimePageable(1,startCriteria,endCriteria);
    }
    
}
