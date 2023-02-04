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
import org.yugo.backend.YuGo.dto.AcumulatedReviewsOut;
import org.yugo.backend.YuGo.dto.AllRideReviewsOut;
import org.yugo.backend.YuGo.dto.ReviewIn;
import org.yugo.backend.YuGo.dto.ReviewOut;
import org.yugo.backend.YuGo.exception.NotFoundException;
import org.yugo.backend.YuGo.model.*;
import org.yugo.backend.YuGo.service.PassengerService;
import org.yugo.backend.YuGo.service.ReviewService;
import org.yugo.backend.YuGo.service.RideService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {
    private final ReviewService reviewService;
    private final RideService rideService;
    private final PassengerService passengerService;

    @Autowired
    public ReviewController(ReviewService reviewService,RideService rideService, PassengerService passengerService){
        this.reviewService=reviewService;
        this.rideService=rideService;
        this.passengerService=passengerService;
    }

    @PostMapping(
            value = "/{rideId}/vehicle",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<ReviewOut> addVehicleReview(@RequestBody @Valid ReviewIn reviewIn,
                                                      @NotNull(message = "Field (id) is required")
                                                      @Positive(message = "Id must be positive")
                                                      @PathVariable(value="rideId") Integer rideId){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Passenger passenger = (Passenger) auth.getPrincipal();
        for(Passenger p:rideService.get(rideId).getPassengers()){
            if (!p.getId().equals(passenger.getId()))
                throw new NotFoundException("Vehicle does not exist!");
        }
        RideReview vehicleReview= new RideReview(reviewIn.getComment(), reviewIn.getRating(),rideService.get(rideId),passengerService.get(passenger.getId()),ReviewType.VEHICLE);
        reviewService.insertRideReview(vehicleReview);
        return new ResponseEntity<>(new ReviewOut(vehicleReview), HttpStatus.OK);
    }


    @PostMapping(
            value = "/{rideId}/driver",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<ReviewOut> addRideReview(@RequestBody @Valid ReviewIn reviewIn,
                                                   @NotNull(message = "Field (rideId) is required")
                                                   @Positive(message = "RideId must be positive")
                                                   @PathVariable(value="rideId") Integer rideId){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Passenger passenger = (Passenger) auth.getPrincipal();
        for(Passenger p:rideService.get(rideId).getPassengers()){
            if (!p.getId().equals(passenger.getId()))
                throw new NotFoundException("Ride does not exist!");
        }
        RideReview rideReview= new RideReview(reviewIn.getComment(), reviewIn.getRating(),rideService.get(rideId),passengerService.get(passenger.getId()),ReviewType.DRIVER);
        reviewService.insertRideReview(rideReview);
        return new ResponseEntity<>(new ReviewOut(rideReview), HttpStatus.OK);
    }

    @GetMapping(
            value = "/vehicle/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'PASSENGER')")
    public ResponseEntity<AllRideReviewsOut> getAllVehicleReviewsByVehicle(@NotNull(message = "Field (id) is required")
                                                                           @Positive(message = "Id must be positive")
                                                                           @PathVariable(value="id") Integer id) {
        List<RideReview> vehicleReviews = reviewService.getRideReviewsByVehicle(id);
        return new ResponseEntity<>(new AllRideReviewsOut(vehicleReviews), HttpStatus.OK);
    }
    @GetMapping(
            value = "driver/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'PASSENGER')")
    public ResponseEntity<AllRideReviewsOut> getAllRideReviewsByDriver(@NotNull(message = "Field (id) is required")
                                                                       @Positive(message = "Id must be positive")
                                                                       @PathVariable(value="id") Integer id){
        List<RideReview> driverReviews = reviewService.getRideReviewsByDriver(id);
        return new ResponseEntity<>(new AllRideReviewsOut(driverReviews), HttpStatus.OK);
    }

    @GetMapping(
            value = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'PASSENGER','DRIVER')")
    public ResponseEntity<List<AcumulatedReviewsOut>> getAllRideReviews(@NotNull(message = "Field (id) is required")
                                                                        @Positive(message = "Id must be positive")
                                                                        @PathVariable(value="id") Integer id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        if (user.getRole().equals("DRIVER")){
            if (!rideService.get(id).getDriver().getId().equals(user.getId())){
                throw new NotFoundException("Ride does not exist!");
            }
        }
        else if (user.getRole().equals("PASSENGER")){
            for(Passenger p:rideService.get(id).getPassengers()){
                if (!p.getId().equals(user.getId()))
                    throw new NotFoundException("Ride does not exist!");
            }
        }

        List<AcumulatedReviewsOut> result= new ArrayList<>();
        for (Passenger passenger:rideService.get(id).getPassengers()) {
            RideReview vehicleReviews = reviewService.getVehicleReviewsByRideByPassenger(id,passenger.getId());
            RideReview driverReviews = reviewService.getDriverReviewsByRideByPassenger(id,passenger.getId());
            AcumulatedReviewsOut acumulatedReviewsOut= new AcumulatedReviewsOut();
            if (vehicleReviews!=null)
                acumulatedReviewsOut.setVehicleReview(new ReviewOut(vehicleReviews));
            if (driverReviews!=null)
                acumulatedReviewsOut.setDriverReview(new ReviewOut(driverReviews));
            result.add(acumulatedReviewsOut);

        }
        if (result.size()==1 && result.get(0).getDriverReview()==null && result.get(0).getVehicleReview()==null)
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        else
            return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
