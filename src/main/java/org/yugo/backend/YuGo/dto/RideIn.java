package org.yugo.backend.YuGo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter @Setter
public class RideIn {
    @Valid
    @NotNull(message = "Field (locations) is required!")
    private List<PathInOut> locations;
    @Valid
    @NotNull(message = "Field (passengers) is required!")
    private List<UserSimplifiedOut> passengers;
    @NotNull(message = "Field (vehicleType) is required!")
    @Size(max = 30, message = "Field (vehicleType) cannot be longer than 30 characters!")
    private String vehicleType;
    @NotNull(message = "Field (babyTransport) is required!")
    private boolean babyTransport;
    @NotNull(message = "Field (petTransport) is required!")
    private boolean petTransport;
    @NotNull(message = "Field (dateTime) is required!")
    @Size(max = 50, message = "Field (dateTime) cannot be longer than 50 characters!")
    private String dateTime;
}
