package org.yugo.backend.YuGo.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.yugo.backend.YuGo.annotation.AuthorizeSelf;
import org.yugo.backend.YuGo.annotation.AuthorizeSelfAndAdmin;
import org.yugo.backend.YuGo.dto.*;
import org.yugo.backend.YuGo.exception.BadRequestException;
import org.yugo.backend.YuGo.exception.NotFoundException;
import org.yugo.backend.YuGo.mapper.FavoritePathMapper;
import org.yugo.backend.YuGo.mapper.PathMapper;
import org.yugo.backend.YuGo.mapper.RideMapper;
import org.yugo.backend.YuGo.model.*;
import org.yugo.backend.YuGo.service.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ride")
public class RideController {
    private final RideService rideService;
    private final PanicService panicService;
    private final PassengerService passengerService;
    private final DriverService driverService;
    private final VehicleService vehicleService;
    private final FavoritePathService favoritePathService;
    private final RoutingService routingService;

    @Autowired
    public RideController(RideService rideService, PanicService panicService, PassengerService passengerService, DriverService driverService, VehicleService vehicleService, FavoritePathService favoritePathService, RoutingService routingService){
        this.rideService=rideService;
        this.panicService=panicService;
        this.passengerService=passengerService;
        this.driverService=driverService;
        this.vehicleService=vehicleService;
        this.favoritePathService=favoritePathService;
        this.routingService = routingService;
    }

    @PostMapping(
            value = "",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<RideDetailedOut> addRide(@RequestBody @Valid RideIn rideIn) throws Exception {
        Ride ride = rideService.createRide(rideIn);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime rideDateTime = LocalDateTime.parse(rideIn.getDateTime(), formatter);
        if(LocalDateTime.now().until(rideDateTime, ChronoUnit.MINUTES) <= 30){
            rideService.searchForDriver(ride.getId());
        }
        return new ResponseEntity<>(RideMapper.fromRidetoDTO(ride), HttpStatus.OK);
    }

    @GetMapping(
            value = "/driver/{id}/active",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('DRIVER')")
    @AuthorizeSelf(pathToUserId = "[0]", message = "Active ride does not exist!")
    public ResponseEntity<RideDetailedOut> getActiveRideByDriver(@NotNull(message = "Field (id) is required")
                                                                  @Positive(message = "Id must be positive")
                                                                  @PathVariable(value="id") Integer id){
        return new ResponseEntity<>(new RideDetailedOut(rideService.getActiveRideByDriver(id)), HttpStatus.OK);
    }

    @GetMapping(
            value = "/passenger/{id}/active",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('PASSENGER')")
    @AuthorizeSelf(pathToUserId = "[0]", message = "Active ride does not exist!")
    public ResponseEntity<RideDetailedOut> getActiveRideByPassenger(@NotNull(message = "Field (id) is required")
                                                                     @Positive(message = "Id must be positive")
                                                                     @PathVariable(value="id") Integer id){
        RideDetailedOut a= new RideDetailedOut(rideService.getActiveRideByPassenger(id));
        return new ResponseEntity<>(a, HttpStatus.OK);
    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN','PASSENGER', 'DRIVER')")
    public ResponseEntity<RideDetailedOut> getRideById(@NotNull(message = "Field (id) is required")
                                                       @Positive(message = "Id must be positive")
                                                       @PathVariable(value="id") Integer id){
        return new ResponseEntity<>(new RideDetailedOut(rideService.get(id)), HttpStatus.OK);
    }

    @PutMapping(
            value = "/{id}/withdraw",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<RideDetailedOut> cancelRide(@NotNull(message = "Field (id) is required")
                                                      @Positive(message = "Id must be positive")
                                                      @PathVariable(value="id") Integer id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Passenger passenger = (Passenger) auth.getPrincipal();
        if (!rideService.get(id).getPassengers().contains(passenger)){
            throw new NotFoundException("Ride does not exist!");
        }
        return new ResponseEntity<>(new RideDetailedOut(rideService.cancelRide(id)), HttpStatus.OK);
    }
    @PutMapping(
            value = "/{id}/start",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideDetailedOut> startRide(@NotNull(message = "Field (id) is required")
                                                     @Positive(message = "Id must be positive")
                                                     @PathVariable(value="id") Integer id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer senderID = ((Driver) auth.getPrincipal()).getId();
        Integer driverID = rideService.get(id).getDriver().getId();
        if (driverID.intValue() != senderID.intValue()){
            throw new NotFoundException("Ride does not exist!");
        }
        return new ResponseEntity<>(new RideDetailedOut(rideService.startRide(id)), HttpStatus.OK);
    }

    @PutMapping(
            value = "/{id}/accept",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideDetailedOut> acceptRide(@NotNull(message = "Field (id) is required")
                                                      @Positive(message = "Id must be positive")
                                                      @PathVariable(value="id") Integer id){
        return new ResponseEntity<>(new RideDetailedOut(rideService.acceptRide(id)), HttpStatus.OK);
    }

    @PutMapping(
            value = "/{id}/end",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideDetailedOut> endRide(@NotNull(message = "Field (id) is required")
                                                   @Positive(message = "Id must be positive")
                                                   @PathVariable(value="id") Integer id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer senderID = ((Driver) auth.getPrincipal()).getId();
        Integer driverID = rideService.get(id).getDriver().getId();
        if (driverID.intValue() != senderID.intValue()){
            throw new NotFoundException("Ride does not exist!");
        }
        return new ResponseEntity<>(new RideDetailedOut(rideService.endRide(id)), HttpStatus.OK);
    }

    @PutMapping(
            value = "/{id}/panic",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('DRIVER', 'PASSENGER')")
    public ResponseEntity<PanicOut> addPanic(@NotNull(message = "Field (id) is required")
                                             @Positive(message = "Id must be positive")
                                             @PathVariable(value="id") Integer id,
                                             @RequestBody @Valid ReasonIn reasonIn){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        if (user.getRole().equals("DRIVER")){
            if (!rideService.get(id).getDriver().equals(user)){
                throw new NotFoundException("Ride does not exist!");
            }
        }
        else if (user.getRole().equals("PASSENGER")){
            if (!rideService.get(id).getPassengers().contains((Passenger) user)){
                throw new NotFoundException("Ride does not exist!");
            }
        }

        Ride ride= rideService.get(id);
        Panic panic= new Panic(user,ride, LocalDateTime.now(), reasonIn.getReason());
        ride.setIsPanicPressed(true);
        rideService.insert(ride);
        panicService.insert(panic);

        return new ResponseEntity<>(new PanicOut(panic), HttpStatus.OK);
    }

    @PutMapping(
            value = "/{id}/cancel",
            produces = MediaType.APPLICATION_JSON_VALUE
    )

    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideDetailedOut> rejectRide(@NotNull(message = "Field (id) is required")
                                                      @Positive(message = "Id must be positive")
                                                      @PathVariable(value="id") Integer id,
                                                      @RequestBody @Valid ReasonIn reasonIn){
        return new ResponseEntity<>(new RideDetailedOut(rideService.rejectRide(id,reasonIn.getReason())), HttpStatus.OK);

    }

    @PostMapping(
            value = "/favorites",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'PASSENGER')")
    public ResponseEntity<FavoritePathOut> addFavoritePath(@RequestBody @Valid FavoritePathIn favoritePathIn){
        Set<Passenger> passengers=new HashSet<>();
        for (UserSimplifiedOut user:favoritePathIn.getPassengers()) {
            passengers.add(passengerService.get(user.getId()));
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        if (favoritePathService.getByPassengerId(user.getId()).size()>10)
            throw new BadRequestException("Number of favorite rides cannot exceed 10!");
        FavoritePath favoritePath= new FavoritePath(favoritePathIn.getFavoriteName(),favoritePathIn.getLocations().stream().map(PathMapper::fromDTOtoPath).collect(Collectors.toList()), passengers,vehicleService.getVehicleTypeByName(favoritePathIn.getVehicleType()),favoritePathIn.getBabyTransport(),favoritePathIn.getPetTransport());
        favoritePath.setOwner(passengerService.get(user.getId()));
        favoritePathService.insert(favoritePath);

        return new ResponseEntity<>(FavoritePathMapper.fromFavoritePathtoDTO(favoritePath), HttpStatus.OK);
    }

    @GetMapping(
            value = "/favorites",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'PASSENGER')")
    public ResponseEntity<List<FavoritePathOut>> getFavoritePathByPassengerId(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        return new ResponseEntity<>(favoritePathService.getByPassengerId(user.getId()).stream()
                .map(FavoritePathMapper::fromFavoritePathtoDTO)
                .toList(), HttpStatus.OK);
    }

    @DeleteMapping(
            value = "/favorites/{id}"
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'PASSENGER')")
    ResponseEntity<Void> deleteFavoritePath(@NotNull(message = "Field (id) is required")
                                            @Positive(message = "Id must be positive")
                                            @PathVariable(value="id") Integer id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Passenger passenger = (Passenger) auth.getPrincipal();
        if (!favoritePathService.get(id).getOwner().equals(passenger)){
            throw new NotFoundException("Favorite path does not exist!");
        }
        favoritePathService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
