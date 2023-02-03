package org.yugo.backend.YuGo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@NoArgsConstructor
@Getter @Setter
@Table(name = "Rides")
public class Ride {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    @Column(name = "start_time")
    private LocalDateTime startTime;
    @Column(name = "end_time")
    private LocalDateTime endTime;
    @Column(name = "price", nullable = false)
    private double totalCost;
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH,CascadeType.MERGE,CascadeType.PERSIST})
    @JoinColumn(name = "driver_id")
    private Driver driver;
    @ManyToMany(fetch = FetchType.LAZY,cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "passenger_rides", joinColumns = @JoinColumn(name = "ride_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "passenger_id", referencedColumnName = "id"))
    private List<Passenger> passengers = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH,CascadeType.MERGE,CascadeType.PERSIST})
    private List<Path> locations;
    @Column(name = "estimated_time")
    private int estimatedTimeInMinutes;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH,mappedBy = "ride")
    private Set<RideReview> rideReviews = new HashSet<RideReview>();
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RideStatus status;
    @JoinColumn(name = "rejection_id")
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.PERSIST, CascadeType.MERGE})
    private Rejection rejection;
    @Column(name = "is_panic_pressed")
    private Boolean isPanicPressed;
    @Column(name = "includes_babies")
    private Boolean babyTransport;
    @Column(name = "includes_pets")
    private Boolean petTransport;
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH,CascadeType.MERGE,CascadeType.PERSIST})
    @JoinColumn(name = "vehicle_type_id")
    private VehicleTypePrice vehicleTypePrice;

    public Ride(LocalDateTime startTime, LocalDateTime endTime, double price, Driver driver, List<Passenger> passengers, List<Path> paths, int estimatedTime, Set<RideReview> rideReviews, RideStatus status, Rejection rejection, Boolean isPanicPressed, Boolean includesBabies, Boolean includesPets, VehicleTypePrice vehicleTypePrice) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalCost = price;
        this.driver = driver;
        this.passengers = passengers;
        this.locations = paths;
        this.estimatedTimeInMinutes = estimatedTime;
        this.rideReviews = rideReviews;
        this.status = status;
        this.rejection = rejection;
        this.isPanicPressed = isPanicPressed;
        this.babyTransport = includesBabies;
        this.petTransport = includesPets;
        this.vehicleTypePrice = vehicleTypePrice;
    }

    public Ride(Ride other) {
        this.id = other.id;
        this.startTime = other.startTime;
        this.endTime = other.endTime;
        this.totalCost = other.totalCost;
        this.driver = other.driver;
        this.passengers = other.passengers;
        this.locations = other.locations;
        this.estimatedTimeInMinutes = other.estimatedTimeInMinutes;
        this.rideReviews = other.rideReviews;
        this.status = other.status;
        this.rejection = other.rejection;
        this.isPanicPressed = other.isPanicPressed;
        this.babyTransport = other.babyTransport;
        this.petTransport = other.petTransport;
        this.vehicleTypePrice = other.vehicleTypePrice;
    }

    public Ride(Integer id, LocalDateTime startTime, LocalDateTime endTime, double totalCost, Driver driver, List<Passenger> passengers,
                List<Path> locations, int estimatedTimeInMinutes, Set<RideReview> rideReviews, RideStatus status, Rejection rejection,
                Boolean isPanicPressed, Boolean babyTransport, Boolean petTransport, VehicleTypePrice vehicleTypePrice) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalCost = totalCost;
        this.driver = driver;
        this.passengers = passengers;
        this.locations = locations;
        this.estimatedTimeInMinutes = estimatedTimeInMinutes;
        this.rideReviews = rideReviews;
        this.status = status;
        this.rejection = rejection;
        this.isPanicPressed = isPanicPressed;
        this.babyTransport = babyTransport;
        this.petTransport = petTransport;
        this.vehicleTypePrice = vehicleTypePrice;
    }
}
