package org.yugo.backend.YuGo.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yugo.backend.YuGo.dto.RideAssumptionIn;
import org.yugo.backend.YuGo.dto.RideAssumptionOut;

@RestController
@RequestMapping("/api/unregisteredUser")
public class UnregisteredUserController {
    @Autowired
    public UnregisteredUserController(){
    }

    @PostMapping(
            value = "",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RideAssumptionOut> getAssumption(@RequestBody @Valid RideAssumptionIn rideAssumptionIn){
        return new ResponseEntity<>(new RideAssumptionOut(10, 450), HttpStatus.OK);
    }
}
