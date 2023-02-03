package org.yugo.backend.YuGo.service;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.util.EnumUtils;
import org.yugo.backend.YuGo.dto.PathInOut;
import org.springframework.transaction.annotation.Transactional;
import org.yugo.backend.YuGo.dto.RideIn;
import org.yugo.backend.YuGo.dto.RouteProperties;
import org.yugo.backend.YuGo.dto.UserSimplifiedOut;
import org.yugo.backend.YuGo.exception.NoContentException;
import org.yugo.backend.YuGo.model.Driver;
import org.yugo.backend.YuGo.model.Ride;
import org.yugo.backend.YuGo.model.User;
import org.yugo.backend.YuGo.model.VehicleType;
import org.yugo.backend.YuGo.exception.BadRequestException;
import org.yugo.backend.YuGo.exception.NotFoundException;
import org.yugo.backend.YuGo.model.*;
import org.yugo.backend.YuGo.repository.RideRepository;
import org.yugo.backend.YuGo.repository.WorkTimeRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
public class RideServiceImpl implements RideService {
    private final RideRepository rideRepository;
    private final RoutingService routingService;
    private final DriverService driverService;
    private final UserService userService;
    private final PassengerService passengerService;
    private final VehicleService vehicleService;
    private final WebSocketService webSocketService;
    private final WorkTimeRepository workTimeRepository;
    @Autowired
    public RideServiceImpl(RideRepository rideRepository, RoutingService routingService, DriverService driverService, UserService userService, PassengerService passengerService, VehicleService vehicleService, WebSocketService webSocketService, WorkTimeRepository workTimeRepository){
        this.rideRepository = rideRepository;
        this.routingService = routingService;
        this.driverService = driverService;
        this.userService = userService;
        this.passengerService=passengerService;
        this.vehicleService = vehicleService;
        this.webSocketService = webSocketService;
        this.workTimeRepository = workTimeRepository;
    }


    @Override
    public Ride save(Ride ride){
        return rideRepository.save(ride);
    }

    @Override
    public Ride createRide(RideIn rideIn){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        if (rideRepository.findUnresolvedRideByPassenger(user.getId()).isPresent())
            throw new BadRequestException("Cannot create a ride while you have one already pending!");

        Ride ride;

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime rideDateTime;
        if (rideIn.getScheduledTime()!=null)
            rideDateTime= LocalDateTime.parse(rideIn.getScheduledTime(), formatter);
        else
            rideDateTime=LocalDateTime.now();
        LocalDateTime now = LocalDateTime.now();

        VehicleTypePrice vehicleTypePrice = vehicleService.getVehicleTypeByName(rideIn.getVehicleType());
        List<Passenger> passengers = new ArrayList<>();
        for(UserSimplifiedOut passenger : rideIn.getPassengers()){
            passengers.add((Passenger) userService.getUser(passenger.getId()));
        }

        RouteProperties routeProperties = getRouteProperties(rideIn);   // contains ride distance (in meters) and estimated duration (in seconds)

        Location rideDestination = new Location();
        rideDestination.setLongitude(rideIn.getLocations().get(0).getDestination().getLongitude());
        rideDestination.setLatitude(rideIn.getLocations().get(0).getDestination().getLatitude());
        rideDestination.setAddress(routingService.reverseAddressSearch(rideDestination.getLatitude(), rideDestination.getLongitude()));

        Location rideDeparture = new Location();
        rideDeparture.setLatitude(rideIn.getLocations().get(0).getDeparture().getLatitude());
        rideDeparture.setLongitude(rideIn.getLocations().get(0).getDeparture().getLongitude());
        rideDeparture.setAddress(routingService.reverseAddressSearch(rideDeparture.getLatitude(), rideDeparture.getLongitude()));


        if(now.until(rideDateTime, ChronoUnit.MINUTES) > 30){
            ride = assembleRide(
                routeProperties,
                rideIn.isBabyTransport(),
                rideIn.isPetTransport(),
                rideDestination,
                rideDeparture,
                vehicleTypePrice,
                passengers,
                RideStatus.SCHEDULED,
                rideDateTime);
        }else{
            ride = assembleRide(
                    routeProperties,
                    rideIn.isBabyTransport(),
                    rideIn.isPetTransport(),
                    rideDestination,
                    rideDeparture,
                    vehicleTypePrice,
                    passengers,
                    RideStatus.PENDING,
                    rideDateTime);
        }
        Ride savedRide = rideRepository.save(ride);
        for(Passenger passenger : ride.getPassengers()){
            this.webSocketService.notifyAddedPassenger(passenger.getId(), savedRide.getId());
        }
        return savedRide;
    }

    @Async
    @Transactional(propagation=REQUIRES_NEW)
    @Override
    public void searchForDriver(Integer id){
        Ride ride = rideRepository.getReferenceById(id);
        Hibernate.initialize(ride);
        Location rideDeparture = ride.getLocations().get(0).getDeparture();
        Location rideDestination = ride.getLocations().get(0).getDestination();
        List<Driver> availableDrivers = filterDrivers(rideDeparture, ride.getVehicleTypePrice().getVehicleType(), ride.getBabyTransport(), ride.getPetTransport(), ride.getPassengers().size());
        if(availableDrivers.isEmpty()){
            for(Passenger passenger : ride.getPassengers()){
                webSocketService.notifyPassengerAboutRide(-1, passenger.getId());
                rideRepository.delete(ride);
                return;
            }
        }
        DriverAvailability driverAvailability = findDriver(availableDrivers, ride.getStartTime(), ride.getEstimatedTimeInMinutes(), rideDeparture, rideDestination);
        if(driverAvailability != null){
            if(driverAvailability.earliestAvailableTime.isBefore(ride.getStartTime())){
                ride.setStartTime(ride.getStartTime());
            }else{
                ride.setStartTime(driverAvailability.earliestAvailableTime);
            }
            ride.setDriver(driverAvailability.driver);
        }else{
            for(Passenger passenger : ride.getPassengers()){
                webSocketService.notifyPassengerAboutRide(-1, passenger.getId());
            }
            rideRepository.delete(ride);
            return;
        }
        rideRepository.save(ride);
        webSocketService.sendRideRequestToDriver(driverAvailability.driver.getId(), ride.getId());
    }
    private List<Driver> filterDrivers(Location rideDeparture, VehicleType vehicleType, boolean isBabyTransport, boolean isPetTransport, int passengerCount){
        List<Driver> availableDrivers = driverService.getDriversInRange(rideDeparture.getLatitude(), rideDeparture.getLongitude(), 10000);
        if(availableDrivers.isEmpty()){
            return availableDrivers;
        }
        availableDrivers.removeIf(driver -> !driver.isOnline());
        if(availableDrivers.isEmpty()){
            return availableDrivers;
        }
        availableDrivers.removeIf(driver -> workTimeRepository.getTotalWorkTimeInLast24Hours(driver.getId()) >= 8);
        if(availableDrivers.isEmpty()){
            return availableDrivers;
        }
        availableDrivers.removeIf(driver -> rideRepository.findAcceptedRideByDriver(driver.getId()).isPresent());
        if(availableDrivers.isEmpty()){
            return availableDrivers;
        }
        availableDrivers.removeIf(driver ->  unproxy(driver.getVehicle()).getVehicleType() != vehicleType ||
                (!unproxy(driver.getVehicle()).getAreBabiesAllowed() && isBabyTransport) ||
                (!unproxy(driver.getVehicle()).getArePetsAllowed() && isPetTransport) ||
                unproxy(driver.getVehicle()).getNumberOfSeats() < passengerCount);
        return availableDrivers;
    }
    private <T> T unproxy(T object){
        return (T) Hibernate.unproxy(object);
    }
    private Ride assembleRide(RouteProperties routeProperties, boolean isBabyTransport, boolean isPetTransport, Location rideDestination, Location rideDeparture, VehicleTypePrice vehicleTypePrice, List<Passenger> passengers, RideStatus rideStatus, LocalDateTime startTime){
        Ride ride = new Ride();
        ride.setStartTime(startTime);
        ride.setStatus(rideStatus);
        ride.setIsPanicPressed(false);
        ride.setEstimatedTimeInMinutes((int) (routeProperties.getDuration() / 60.0));
        ride.setBabyTransport(isBabyTransport);
        ride.setPetTransport(isPetTransport);

        List<Path> paths = new ArrayList<>();
        Path newRidePath = new Path();
        newRidePath.setDestination(rideDestination);
        newRidePath.setDeparture(rideDeparture);
        paths.add(newRidePath);
        ride.setLocations(paths);

        ride.setTotalCost(vehicleTypePrice.getPricePerKM() * routeProperties.getDistance() / 1000.0);
        ride.setVehicleTypePrice(vehicleTypePrice);

        ride.setPassengers(passengers);

        return ride;
    }
    private class DriverAvailability{
        public Driver driver;
        public LocalDateTime earliestAvailableTime;
    }
    private DriverAvailability findDriver(List<Driver> drivers, LocalDateTime rideStartDateTime, double rideEstTime, Location rideDeparture, Location rideDestination){
        class DriverSortingObject{
            public LocalDateTime earliestAvailableTime;
            public LocalDateTime nextRideTime;
            public LocalDateTime newRideEndEstTime;     // Time instance at which driver will arrive from new ride destination to next ride departure location
            public int index;
        }
        List<DriverSortingObject> sortingTempArray = new ArrayList<>();
        for(int i = 0; i < drivers.size(); i++){
            Driver driver = drivers.get(i);
            DriverSortingObject obj = new DriverSortingObject();
            Optional<Ride> currentRide = rideRepository.findActiveRideByDriver(driver.getId());
            if(currentRide.isPresent()){
                Location currentRideDestination = (Location) Hibernate.unproxy(currentRide.get().getLocations().get(0).getDestination());
                RouteProperties routeProperties = routingService.getRouteProperties(currentRideDestination.getLatitude(),currentRideDestination.getLongitude(), rideDeparture.getLatitude(), rideDeparture.getLongitude());
                LocalDateTime earliestAvailableTime = currentRide.get().getStartTime().plusMinutes(currentRide.get().getEstimatedTimeInMinutes()).plusMinutes((long) ((routeProperties.getDuration() / 60) + 5));
                if(earliestAvailableTime.isBefore(LocalDateTime.now())){
                    obj.earliestAvailableTime = LocalDateTime.now().plusMinutes(currentRide.get().getEstimatedTimeInMinutes()).plusMinutes((long) ((routeProperties.getDuration() / 60) + 5));
                }else{
                    obj.earliestAvailableTime = earliestAvailableTime;
                }
            }else{
                Location currentDriverLocation = (Location) Hibernate.unproxy(driver.getVehicle().getCurrentLocation());
                RouteProperties routeProperties = routingService.getRouteProperties(currentDriverLocation.getLatitude(), currentDriverLocation.getLongitude(), rideDeparture.getLatitude(), rideDeparture.getLongitude());
                obj.earliestAvailableTime = LocalDateTime.now().plusMinutes((long) ((routeProperties.getDuration() / 60) + 5));
            }
            Optional<Ride> rideOpt = rideRepository.getNextRide(driver.getId());
            if(rideOpt.isPresent()){
                RouteProperties routeProperties = routingService.getRouteProperties(rideDestination.getLatitude(), rideDestination.getLongitude(), rideOpt.get().getLocations().get(0).getDeparture().getLatitude(), rideOpt.get().getLocations().get(0).getDeparture().getLongitude());
                if(obj.earliestAvailableTime.isBefore(rideStartDateTime)){
                    obj.newRideEndEstTime = rideStartDateTime.plusMinutes((long) (routeProperties.getDuration() / 60 + 5 + rideEstTime));   //adding 5 minutes as extra time
                }else{
                    obj.newRideEndEstTime = obj.earliestAvailableTime.plusMinutes((long) (routeProperties.getDuration() / 60 + 5 + rideEstTime));
                }
            }
            obj.nextRideTime = rideOpt.map(Ride::getStartTime).orElse(null);
            obj.index = i;
            sortingTempArray.add(obj);
        }
        sortingTempArray.sort((o1, o2) -> {
            if (o1.earliestAvailableTime.isBefore(o2.earliestAvailableTime)) {
                return -1;
            } else if (o2.earliestAvailableTime.isBefore(o1.earliestAvailableTime)) {
                return 1;
            }
            return 0;
        });
        DriverAvailability output = new DriverAvailability();
        for(DriverSortingObject obj : sortingTempArray){
            if(obj.newRideEndEstTime == null || obj.newRideEndEstTime.isBefore(obj.nextRideTime)){
                output.driver = drivers.get(obj.index);
                output.earliestAvailableTime = obj.earliestAvailableTime;
                return output;
            }
        }
        return null;
    }

    private RouteProperties getRouteProperties(RideIn rideIn){
        double fromLat = rideIn.getLocations().get(0).getDeparture().getLatitude();
        double fromLng = rideIn.getLocations().get(0).getDeparture().getLongitude();
        double toLat = rideIn.getLocations().get(0).getDestination().getLatitude();
        double toLng = rideIn.getLocations().get(0).getDestination().getLongitude();;
        return routingService.getRouteProperties(fromLat, fromLng, toLat, toLng);
    }


    @Override
    public List<Ride> getAll() {
        return rideRepository.findAll();
    }

    @Override
    public List<Ride> getAllByDate(LocalDateTime from, LocalDateTime to) {
        return rideRepository.findAllByDate(from,to);
    }

    @Override
    public Ride get(Integer id) {
        Optional<Ride> ride=  rideRepository.findById(id);
        if (ride.isEmpty())
            throw new NotFoundException("Ride does not exist!");
        return ride.get();
    }

    @Override
    public Ride getActiveRideByDriver(Integer id){
        Optional<Ride> ride=  rideRepository.findActiveRideByDriver(id);
        if (ride.isEmpty())
            throw new NoContentException("Active ride does not exist");
        return ride.get();
    }

    @Override
    public Ride getActiveRideByPassenger(Integer id){
        Optional<Ride> ride=  rideRepository.findActiveRideByPassenger(id);
        if (ride.isEmpty())
            throw new NotFoundException("Active ride does not exist");
        return ride.get();
    }
    @Override
    public Page<Ride> getPassengerRides(Integer passengerId, LocalDateTime from, LocalDateTime to, Pageable page){
        passengerService.get(passengerId);
        return rideRepository.findRidesByPassenger(passengerId, from, to, page);
    }
    @Override
    public List<Ride> getPassengerRidesNonPageable(Integer passengerId, LocalDateTime from, LocalDateTime to){
        passengerService.get(passengerId);
        return rideRepository.findRidesByPassenger(passengerId, from, to);
    }
    @Override
    public Page<Ride> getUserRides(Integer userId, LocalDateTime from, LocalDateTime to, Pageable page){
        userService.getUser(userId);
        return rideRepository.findRidesByUser(userId, from, to, page);
    }

    @Override
    public Ride getUnresolvedRide(Integer userID){
        Optional<Ride> ride = rideRepository.findUnresolvedRideByPassenger(userID);
        if(ride.isPresent()){
            return ride.get();
        }
        else {
            throw new NotFoundException("There are not unresolved rides");
        }
    }

    @Override
    public void notifyPassengersThatVehicleHasArrived(Integer rideID){
        Ride ride = get(rideID);
        for(Passenger passenger : ride.getPassengers()){
            webSocketService.notifyPassengerThatVehicleHasArrived(passenger.getId(), rideID);
        }
    }

    public Page<Ride> getRidesByDriverPage(Integer driverId, Pageable page, LocalDateTime start, LocalDateTime end){
        driverService.getDriver(driverId);
        return rideRepository.findRidesByDriverAndStartTimeAndEndTimePageable(driverId, page, start, end);
    }
    @Override
    public List<Ride> getRidesByDriverNonPageable(Integer driverId, LocalDateTime start, LocalDateTime end){
        driverService.getDriver(driverId);
        return rideRepository.findRidesByDriverAndStartTimeAndEndTimePageable(driverId, start, end);
    }

    public Ride cancelRide(Integer id){
        Ride ride = get(id);
        if (ride.getStatus()== RideStatus.ACTIVE || ride.getStatus()==RideStatus.PENDING || ride.getStatus()==RideStatus.SCHEDULED) {
            ride.setStatus(RideStatus.CANCELED);
            save(ride);
        }
        else{
            throw new BadRequestException("Cannot cancel a ride that is not in status PENDING or STARTED!");
        }
        return ride;
    }

    public Ride startRide(Integer id){
        Ride ride =get(id);
        if (ride.getStatus()== RideStatus.ACCEPTED) {
            ride.setStatus(RideStatus.ACTIVE);
            for (Passenger passenger : ride.getPassengers()){
                webSocketService.notifyPassengerAboutRideStart(passenger.getId());
            }
            save(ride);
        }
        else{
            throw new BadRequestException("Cannot start a ride that is not in status ACCEPTED!");
        }
        return ride;
    }
    public Ride acceptRide(Integer id){
        Ride ride = get(id);
        if (ride.getStatus() == RideStatus.PENDING || ride.getStatus() == RideStatus.SCHEDULED) {
            ride.setStatus(RideStatus.ACCEPTED);
            save(ride);
            for(Passenger passenger : ride.getPassengers()){
                webSocketService.notifyPassengerAboutRide(ride.getId(), passenger.getId());
            }
        }
        else{
            throw new BadRequestException("Cannot accept a ride that is not in status PENDING!");
        }
        return ride;
    }
    public Ride endRide(Integer id){
        Ride ride = get(id);
        if (ride.getStatus() == RideStatus.ACTIVE) {
            ride.setStatus(RideStatus.FINISHED);
            ride.setEndTime(LocalDateTime.now());
            save(ride);
            for (Passenger passenger : ride.getPassengers()){
                webSocketService.notifyPassengerAboutRideEnd(passenger.getId(),id);
            }
        }
        else{
            throw new BadRequestException("Cannot end a ride that is not in status ACTIVE!");
        }
        return ride;
    }
    public Ride rejectRide(Integer id,String reason){
        Ride ride = get(id);
        if (ride.getStatus() == RideStatus.PENDING || ride.getStatus() == RideStatus.SCHEDULED) {
            ride.setStatus(RideStatus.REJECTED);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            Rejection rejection = new Rejection(ride,user,reason,LocalDateTime.now());
            ride.setRejection(rejection);
            save(ride);
            for(Passenger passenger : ride.getPassengers()){
                webSocketService.notifyPassengerAboutRide(ride.getId(), passenger.getId());
            }
        }
        else{
            throw new BadRequestException("Cannot accept a ride that is not in status PENDING!");
        }
        return ride;
    }
}
