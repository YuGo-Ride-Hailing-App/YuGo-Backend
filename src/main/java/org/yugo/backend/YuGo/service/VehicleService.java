package org.yugo.backend.YuGo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.yugo.backend.YuGo.dto.LocationInOut;
import org.yugo.backend.YuGo.model.Location;
import org.yugo.backend.YuGo.model.Vehicle;
import org.yugo.backend.YuGo.model.VehicleChangeRequest;
import org.yugo.backend.YuGo.model.VehicleTypePrice;

import java.util.List;

public interface VehicleService {
    /* =========================== Vehicle =========================== */
    Vehicle insertVehicle(Vehicle vehicle);
    List<Vehicle> getAllVehicles();
    List<Vehicle> getAllVehiclesWithDriver();
    Vehicle getVehicle(Integer id);
    Integer getVehiclesDriver(Integer id);

    void updateVehicleLocation(Location location, Integer vehicleID);

    Vehicle updateVehicle(Vehicle vehicle);
    /* =========================== VehicleType =========================== */
    VehicleTypePrice insertVehicleType(VehicleTypePrice vehicleTypePrice);
    List<VehicleTypePrice> getAllVehicleTypes();
    VehicleTypePrice getVehicleType(Integer id);
    public VehicleTypePrice getVehicleTypeByName(String name);

    /* =========================== VehicleChangeRequest =========================== */
    VehicleChangeRequest insertVehicleChangeRequest(VehicleChangeRequest vehicleChangeRequest);
    Page<VehicleChangeRequest> getAllVehicleChangeRequests(Pageable page);
    void acceptVehicleChangeRequest(Integer requestId);
    void rejectVehicleChangeRequest(Integer requestId);
}
