package org.yugo.backend.YuGo.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.yugo.backend.YuGo.exception.NoContentException;
import org.yugo.backend.YuGo.exception.NotFoundException;
import org.yugo.backend.YuGo.model.*;
import org.yugo.backend.YuGo.repository.DriverRepository;
import org.yugo.backend.YuGo.repository.RideRepository;
import org.yugo.backend.YuGo.repository.WorkTimeRepository;
import org.yugo.backend.YuGo.service.*;

import java.time.LocalDateTime;
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
    public void shouldFindRideByID(){
        LocalDateTime start= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime end= LocalDateTime.of(2022,5,5,5,15);

        Ride ride = new Ride(1,start,end,15,null,null,null,10,null,RideStatus.PENDING,null,false,false,false,null);
        Mockito.when(rideRepository.findById(1)).thenReturn(Optional.of(ride));

        Ride actualRide= rideService.get(1);

        Assertions.assertEquals(actualRide.getId(),ride.getId());
        Assertions.assertEquals(start,ride.getStartTime());
        Assertions.assertEquals(end,ride.getEndTime());
        verify(rideRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should get ride negative id not exist")
    public void shouldFindRideByIDNotFound(){
        Mockito.when(rideRepository.findById(2)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class,()->{rideService.get(2);},"Ride does not exist!");
        verify(rideRepository, times(1)).findById(2);
    }

    @Test
    @DisplayName("Should get ride negative id null")
    public void shouldFindRideByIDNull(){
        Mockito.when(rideRepository.findById(null)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class,()->{rideService.get(null);},"Ride does not exist!");
        verify(rideRepository, times(1)).findById(null);
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
        verify(rideRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get all rides positive empty")
    public void shouldGetAllRidesEmpty(){
        LocalDateTime start= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime end= LocalDateTime.of(2022,5,5,5,15);

        Mockito.when(rideRepository.findAll()).thenReturn(new ArrayList<>());

        List<Ride> actualRides= rideService.getAll();

        Assertions.assertEquals(0,actualRides.size());
        verify(rideRepository, times(1)).findAll();
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
        verify(rideRepository, times(1)).findAllByDate(startCriteria,endCriteria);
    }

    @Test
    @DisplayName("Should get all rides by date positive empty")
    public void shouldGetAllRidesByDateEmpty(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,5,5,15);

        Mockito.when(rideRepository.findAllByDate(startCriteria,endCriteria)).thenReturn(new ArrayList<>());

        List<Ride> actualRides= rideService.getAllByDate(startCriteria,endCriteria);

        Assertions.assertEquals(0,actualRides.size());
        verify(rideRepository, times(1)).findAllByDate(startCriteria,endCriteria);
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
        verify(rideRepository, times(1)).findActiveRideByDriver(1);
    }

    @Test
    @DisplayName("Should get active ride by non existing driver")
    public void shouldntFindDriverNonExisting(){
        LocalDateTime start= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime end= LocalDateTime.of(2022,5,5,5,15);
        Driver driver=new Driver();
        driver.setId(1);
        Ride ride = new Ride(1,start,end,15,driver,null,null,10,null,RideStatus.ACTIVE,null,false,false,false,null);
        Mockito.when(driverService.getDriver(2)).thenThrow(NotFoundException.class);

        Assertions.assertThrows(NotFoundException.class,()->{rideService.getActiveRideByDriver(2);});
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
        verify(rideRepository, times(1)).findActiveRideByDriver(1);
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
        verify(rideRepository, times(1)).findActiveRideByPassenger(1);
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
        Mockito.when(passengerService.get(2)).thenThrow(NotFoundException.class);

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

        Assertions.assertThrows(NoContentException.class,()->{rideService.getActiveRideByPassenger(1);});
        verify(rideRepository, times(1)).findActiveRideByPassenger(1);
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

        Mockito.when(rideRepository.findRidesByPassenger(1,startCriteria,endCriteria,PageRequest.of(0, 5))).thenReturn(page);

        Page<Ride> actualRides= rideService.getPassengerRides(1,startCriteria,endCriteria,PageRequest.of(0, 5));
        Assertions.assertEquals(page.getTotalElements(),actualRides.getTotalElements());
        Assertions.assertEquals(page.getTotalPages(),actualRides.getTotalPages());
        verify(rideRepository, times(1)).findRidesByPassenger(1,startCriteria,endCriteria,PageRequest.of(0, 5));
    }

    @Test
    @DisplayName("Should get all rides by passenger non exist")
    public void shouldGetAllRidesByPassengerNonExisting(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,5,5,15);
        Mockito.when(passengerService.get(2)).thenThrow(NotFoundException.class);

        Assertions.assertThrows(NotFoundException.class,()->{rideService.getPassengerRides(2,startCriteria,endCriteria,PageRequest.of(0, 5));},"Passenger does not exist!");
        verify(rideRepository, times(0)).findRidesByPassenger(2,startCriteria,endCriteria,PageRequest.of(0, 5));
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
        verify(rideRepository, times(1)).findRidesByPassenger(1,startCriteria,endCriteria,PageRequest.of(0, 5));
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
        verify(rideRepository, times(1)).findRidesByPassenger(1,startCriteria,endCriteria);
    }

    @Test
    @DisplayName("Should get all rides by passenger non pagable non exist")
    public void shouldGetAllRidesByPassengerNonExistingNonPagable(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,5,5,15);
        Mockito.when(passengerService.get(2)).thenThrow(NotFoundException.class);

        Assertions.assertThrows(NotFoundException.class,()->{rideService.getPassengerRidesNonPageable(2,startCriteria,endCriteria);},"Passenger does not exist!");
        verify(rideRepository, times(0)).findRidesByPassenger(2,startCriteria,endCriteria);
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
        verify(rideRepository, times(1)).findRidesByPassenger(1,startCriteria,endCriteria);
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
        verify(rideRepository, times(1)).findRidesByUser(1,startCriteria,endCriteria,PageRequest.of(0, 5));
    }

    @Test
    @DisplayName("Should get all rides by user non exist")
    public void shouldGetAllRidesByUserNonExisting(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,5,5,15);
        Mockito.when(userService.getUser(2)).thenThrow(NotFoundException.class);

        Assertions.assertThrows(NotFoundException.class,()->{rideService.getUserRides(2,startCriteria,endCriteria,PageRequest.of(0, 5));},"User does not exist!");
        verify(rideRepository, times(0)).findRidesByUser(2,startCriteria,endCriteria,PageRequest.of(0, 5));
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
        verify(rideRepository, times(1)).findRidesByUser(1,startCriteria,endCriteria,PageRequest.of(0, 5));
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
        verify(rideRepository, times(1)).findUnresolvedRideByPassenger(1);
    }

    @Test
    @DisplayName("Should get unresolved ride negative id not exist")
    public void shouldFindUnresolvedRideByIDNotFound(){
        Mockito.when(rideRepository.findUnresolvedRideByPassenger(2)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class,()->{rideService.getUnresolvedRide(2);},"Ride does not exist!");
        verify(rideRepository, times(1)).findUnresolvedRideByPassenger(2);
    }

    @Test
    @DisplayName("Should get unresolved ride negative id null")
    public void shouldFindUnresolvedRideByIDNull(){
        Mockito.when(rideRepository.findUnresolvedRideByPassenger(null)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class,()->{rideService.getUnresolvedRide(null);},"Ride does not exist!");
        verify(rideRepository, times(1)).findUnresolvedRideByPassenger(null);
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
        verify(rideRepository, times(1)).findRidesByDriverAndStartTimeAndEndTimePageable(1,PageRequest.of(0, 5),startCriteria,endCriteria);
    }

    @Test
    @DisplayName("Should get all rides by driver non exist")
    public void shouldGetAllRidesByDriverNonExisting(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,5,5,15);
        Mockito.when(driverService.getDriver(2)).thenThrow(NotFoundException.class);

        Assertions.assertThrows(NotFoundException.class,()->{rideService.getRidesByDriverPage(2,PageRequest.of(0, 5),startCriteria,endCriteria);},"Passenger does not exist!");
        verify(rideRepository, times(0)).findRidesByDriverAndStartTimeAndEndTimePageable(2,PageRequest.of(0, 5),startCriteria,endCriteria);
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
        verify(rideRepository, times(1)).findRidesByDriverAndStartTimeAndEndTimePageable(1,PageRequest.of(0, 5),startCriteria,endCriteria);
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
        verify(rideRepository, times(1)).findRidesByDriverAndStartTimeAndEndTimePageable(1,startCriteria,endCriteria);
    }

    @Test
    @DisplayName("Should get all rides by driver non pagable non exist")
    public void shouldGetAllRidesByDriverNonExistingNonPagable(){
        LocalDateTime startCriteria= LocalDateTime.of(2022,5,5,5,5);
        LocalDateTime endCriteria= LocalDateTime.of(2022,5,5,5,15);
        Mockito.when(driverService.getDriver(2)).thenThrow(NotFoundException.class);

        Assertions.assertThrows(NotFoundException.class,()->{rideService.getRidesByDriverNonPageable(2,startCriteria,endCriteria);},"Passenger does not exist!");
        verify(rideRepository, times(0)).findRidesByDriverAndStartTimeAndEndTimePageable(2,startCriteria,endCriteria);
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
        verify(rideRepository, times(1)).findRidesByDriverAndStartTimeAndEndTimePageable(1,startCriteria,endCriteria);
    }


}
