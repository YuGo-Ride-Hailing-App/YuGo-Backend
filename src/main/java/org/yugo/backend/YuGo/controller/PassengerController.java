package org.yugo.backend.YuGo.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yugo.backend.YuGo.dto.AllPassengersOut;
import org.yugo.backend.YuGo.dto.AllRidesOut;
import org.yugo.backend.YuGo.dto.UserDetailedIn;
import org.yugo.backend.YuGo.dto.UserDetailedInOut;
import org.yugo.backend.YuGo.mapper.UserDetailedMapper;
import org.yugo.backend.YuGo.model.Passenger;
import org.yugo.backend.YuGo.model.Ride;
import org.yugo.backend.YuGo.service.PassengerService;
import org.yugo.backend.YuGo.service.RideService;
import org.yugo.backend.YuGo.service.UserActivationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

@RestController
@RequestMapping("/api/passenger")
public class PassengerController {
    private final PassengerService passengerService;
    private final RideService rideService;
    private final UserActivationService userActivationService;

    @Autowired
    public PassengerController(PassengerService passengerService, RideService rideService,
                               UserActivationService userActivationService){
        this.passengerService = passengerService;
        this.rideService = rideService;
        this.userActivationService = userActivationService;
    }

    @PostMapping(
            value = "",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserDetailedInOut> createPassenger(@RequestBody @Valid UserDetailedIn user){
        Passenger passenger = new Passenger(user);
        passengerService.insert(passenger);
        return new ResponseEntity<>(UserDetailedMapper.fromUsertoDTO(passenger), HttpStatus.OK);
    }

    @GetMapping(
            value = "",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AllPassengersOut> getAllPassengers(@Min(value=0, message = "Page must be positive")
                                                             @NotNull(message = "Field (page) is required")
                                                             @RequestParam int page,
                                                             @Positive(message = "Size must be positive")
                                                             @NotNull(message = "Field (size) is required")
                                                             @RequestParam int size){
        Page<Passenger> passengers = passengerService.getPassengersPage(PageRequest.of(page, size));
        return new ResponseEntity<>(new AllPassengersOut(passengers), HttpStatus.OK);
    }

    @GetMapping(
            value = "/activate/{activationId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> activatePassenger(@NotNull(message = "Field (id) is required")
                                               @Positive(message = "Id must be positive")
                                               @PathVariable(value="id") Integer activationId){
        userActivationService.activateUser(activationId);
        HashMap<String, String> response = new HashMap<>();
        response.put("message", "Successful account activation!");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'PASSENGER','DRIVER')")
    public ResponseEntity<UserDetailedInOut> getPassenger(@NotNull(message = "Field (id) is required")
                                                          @Positive(message = "Id must be positive")
                                                          @PathVariable(value="id") Integer id){
        return new ResponseEntity<>(UserDetailedMapper.fromUsertoDTO(passengerService.get(id)), HttpStatus.OK);
    }

    @PutMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'PASSENGER')")
    public ResponseEntity<UserDetailedInOut> updatePassenger(@RequestBody @Valid UserDetailedIn updatedUserDTO,
                                                             @NotNull(message = "Field (id) is required")
                                                             @Positive(message = "Id must be positive")
                                                             @PathVariable(value="id") Integer id){
        Passenger passengerUpdate = new Passenger(updatedUserDTO);
        passengerUpdate.setId(id);
        Passenger updatedPassenger = passengerService.update(passengerUpdate);
        return new ResponseEntity<>(UserDetailedMapper.fromUsertoDTO(updatedPassenger), HttpStatus.OK);
    }

    @GetMapping(
            value = "/{id}/ride",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'PASSENGER')")
    ResponseEntity<AllRidesOut> getPassengerRides(@NotNull(message = "Field (id) is required")
                                                  @Positive(message = "Id must be positive")
                                                  @PathVariable(value="id") Integer id,
                                                  @Min(value=0, message = "Page must be positive")
                                                  @NotNull(message = "Field (page) is required")
                                                  @RequestParam(name="page") int page,
                                                  @Positive(message = "Size must be positive")
                                                  @NotNull(message = "Field (size) is required")
                                                  @RequestParam(name="size") int size,
                                                  @RequestParam(name = "sort", required = false) String sort,
                                                  @NotBlank(message = "Field (from) is required")
                                                  @RequestParam(name = "from") String from,
                                                  @NotBlank(message = "Field (to) is required")
                                                  @RequestParam(name = "to") String to) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime fromTime = LocalDate.parse(from, formatter).atTime(LocalTime.MIDNIGHT);
        LocalDateTime toTime = LocalDate.parse(to, formatter).atTime(LocalTime.MIDNIGHT);
        Page<Ride> rides = rideService.getPassengerRides(id, fromTime, toTime,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC,sort)));

        return new ResponseEntity<>(new AllRidesOut(rides), HttpStatus.OK);
    }
}
