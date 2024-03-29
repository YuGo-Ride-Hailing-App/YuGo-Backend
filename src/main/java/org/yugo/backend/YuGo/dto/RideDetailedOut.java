package org.yugo.backend.YuGo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.yugo.backend.YuGo.mapper.PathMapper;
import org.yugo.backend.YuGo.mapper.UserSimplifiedMapper;
import org.yugo.backend.YuGo.model.Ride;

import java.time.LocalDateTime;
import java.util.List;
@Getter @Setter
@NoArgsConstructor
public class RideDetailedOut {
    private Integer id;
    private List<PathInOut> locations;
    private String startTime;
    private String endTime;
    private Double totalCost;
    private UserSimplifiedOut driver;
    private List<UserSimplifiedOut> passengers;
    private int estimatedTimeInMinutes;
    private String vehicleType;
    private boolean babyTransport;
    private boolean petTransport;
    private RejectionOut rejection;
    private String status;
    private String scheduledTime;

    public RideDetailedOut(Ride ride) {
        this.id= ride.getId();
        if(ride.getStartTime() != null){
            this.startTime = ride.getStartTime().toString();
        }else{
            this.startTime = null;
        }
        if(ride.getEndTime() != null){
            this.endTime = ride.getEndTime().toString();
        }else{
            this.endTime = null;
        }
        this.totalCost = ride.getTotalCost();
        if(ride.getDriver() != null){
            this.driver = UserSimplifiedMapper.fromUsertoDTO(ride.getDriver());
        }else{
            this.driver = null;
        }
        this.passengers = ride.getPassengers().stream().map(UserSimplifiedMapper::fromUsertoDTO).toList();
        this.estimatedTimeInMinutes = ride.getEstimatedTimeInMinutes();
        this.vehicleType = ride.getVehicleTypePrice().getVehicleType().toString();
        this.babyTransport = ride.getBabyTransport();
        this.petTransport = ride.getPetTransport();
        this.locations = ride.getLocations().stream().map(PathMapper::fromPathtoDTO).toList();
        this.status = ride.getStatus().toString();
        if (ride.getRejection()!=null)
            this.rejection=new RejectionOut(ride.getRejection());
        this.scheduledTime=ride.getStartTime().toString();
    }
}
