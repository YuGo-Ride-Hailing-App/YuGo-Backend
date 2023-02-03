package org.yugo.backend.YuGo.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.yugo.backend.YuGo.model.*;
import org.yugo.backend.YuGo.repository.RideRepository;
import org.yugo.backend.YuGo.repository.WorkTimeRepository;
import org.yugo.backend.YuGo.service.*;

import java.time.LocalDateTime;
import java.util.Optional;

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



}
