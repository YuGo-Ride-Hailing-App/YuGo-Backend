package org.yugo.backend.YuGo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yugo.backend.YuGo.dto.ReportOut;
import org.yugo.backend.YuGo.dto.StatisticsOut;
import org.yugo.backend.YuGo.model.Location;
import org.yugo.backend.YuGo.model.Ride;
import org.yugo.backend.YuGo.model.RideReview;
import org.yugo.backend.YuGo.model.WorkTime;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService{
    private final PassengerService passengerService;
    private final RideService rideService;
    private final RoutingService routingService;
    private final ReviewService reviewService;
    private final DriverService driverService;

    @Autowired
    public ReportServiceImpl(PassengerService passengerService,RideService rideService, RoutingService routingService, ReviewService reviewService,DriverService driverService){
        this.passengerService=passengerService;
        this.rideService=rideService;
        this.routingService=routingService;
        this.reviewService=reviewService;
        this.driverService=driverService;
    }
    @Override
    public ReportOut getNumberOfRidesByUser(Integer userId, LocalDateTime from, LocalDateTime to){
        List<Ride> rides;
        try{
            rides=rideService.getPassengerRidesNonPageable(userId,from,to);
        }catch (Exception e){
            rides=rideService.getRidesByDriverNonPageable(userId,from,to);
        }
        HashMap<LocalDate,Double> result= new HashMap<LocalDate, Double>();
        for (LocalDate date = from.toLocalDate(); date.isBefore(to.toLocalDate()); date = date.plusDays(1))
        {
            result.put(date, 0.0);
        }
        for (Ride ride:rides) {
            result.put(ride.getStartTime().toLocalDate(), result.get(ride.getStartTime().toLocalDate())+1);
        }
        ArrayList<LocalDate> sortedKeys
                = new ArrayList<LocalDate>(result.keySet());

        Collections.sort(sortedKeys);
        ArrayList<Double> sortedValues=new ArrayList<>();
        for (LocalDate key:sortedKeys) {
            sortedValues.add(result.get(key));
        }

        return new ReportOut(sortedKeys,sortedValues);


    }

    @Override
    public ReportOut getTotalNumberOfRides(LocalDateTime from, LocalDateTime to){
        List<Ride> rides=rideService.getAllByDate(from,to);
        HashMap<LocalDate,Double> result= new HashMap<LocalDate, Double>();
        for (LocalDate date = from.toLocalDate(); date.isBefore(to.toLocalDate()); date = date.plusDays(1))
        {
            result.put(date, 0.0);
        }
        for (Ride ride:rides) {
            result.put(ride.getStartTime().toLocalDate(), result.get(ride.getStartTime().toLocalDate())+1);
        }
        ArrayList<LocalDate> sortedKeys
                = new ArrayList<LocalDate>(result.keySet());

        Collections.sort(sortedKeys);
        ArrayList<Double> sortedValues=new ArrayList<>();
        for (LocalDate key:sortedKeys) {
            sortedValues.add(result.get(key));
        }

        return new ReportOut(sortedKeys,sortedValues);


    }

    @Override
    public ReportOut getTotalCostOfRidesByUser(Integer userId, LocalDateTime from, LocalDateTime to){
        List<Ride> rides;
        try{
            rides=rideService.getPassengerRidesNonPageable(userId,from,to);
        }catch (Exception e){
            rides=rideService.getRidesByDriverNonPageable(userId,from,to);
        }
        HashMap<LocalDate,Double> result= new HashMap<LocalDate, Double>();
        for (LocalDate date = from.toLocalDate(); date.isBefore(to.toLocalDate()); date = date.plusDays(1))
        {
            result.put(date, 0.0);
        }
        for (Ride ride:rides) {
            result.put(ride.getStartTime().toLocalDate(), result.get(ride.getStartTime().toLocalDate())+ride.getTotalCost());
        }
        ArrayList<LocalDate> sortedKeys
                = new ArrayList<LocalDate>(result.keySet());

        Collections.sort(sortedKeys);
        ArrayList<Double> sortedValues=new ArrayList<>();
        for (LocalDate key:sortedKeys) {
            sortedValues.add(result.get(key));
        }

        return new ReportOut(sortedKeys,sortedValues);


    }

    @Override
    public ReportOut getTotalCostOfRides(LocalDateTime from, LocalDateTime to){
        List<Ride> rides=rideService.getAllByDate(from,to);
        HashMap<LocalDate,Double> result= new HashMap<LocalDate, Double>();
        for (LocalDate date = from.toLocalDate(); date.isBefore(to.toLocalDate()); date = date.plusDays(1))
        {
            result.put(date, 0.0);
        }
        for (Ride ride:rides) {
            result.put(ride.getStartTime().toLocalDate(), result.get(ride.getStartTime().toLocalDate())+ride.getTotalCost());

        }
        ArrayList<LocalDate> sortedKeys
                = new ArrayList<LocalDate>(result.keySet());

        Collections.sort(sortedKeys);
        ArrayList<Double> sortedValues=new ArrayList<>();
        for (LocalDate key:sortedKeys) {
            sortedValues.add(result.get(key));
        }

        return new ReportOut(sortedKeys,sortedValues);


    }

    @Override
    public ReportOut getDistanceByUser(Integer userId, LocalDateTime from, LocalDateTime to){
        List<Ride> rides;
        try{
            rides=rideService.getPassengerRidesNonPageable(userId,from,to);
        }catch (Exception e){
            rides=rideService.getRidesByDriverNonPageable(userId,from,to);
        }
        HashMap<LocalDate,Double> result= new HashMap<LocalDate, Double>();
        for (LocalDate date = from.toLocalDate(); date.isBefore(to.toLocalDate()); date = date.plusDays(1))
        {
            result.put(date, 0.0);
        }
        for (Ride ride:rides) {
            Double distance=routingService.getRouteProperties(ride.getLocations().get(0).getDeparture().getLatitude(),ride.getLocations().get(0).getDeparture().getLongitude(),ride.getLocations().get(0).getDestination().getLatitude(),ride.getLocations().get(0).getDestination().getLongitude()).getDistance()/1000;
            result.put(ride.getStartTime().toLocalDate(), result.get(ride.getStartTime().toLocalDate())+distance);
        }
        ArrayList<LocalDate> sortedKeys
                = new ArrayList<LocalDate>(result.keySet());

        Collections.sort(sortedKeys);
        ArrayList<Double> sortedValues=new ArrayList<>();
        for (LocalDate key:sortedKeys) {
            sortedValues.add(result.get(key));
        }

        return new ReportOut(sortedKeys,sortedValues);


    }

    @Override
    public ReportOut getTotalDistance( LocalDateTime from, LocalDateTime to){
        List<Ride> rides=rideService.getAllByDate(from,to);
        HashMap<LocalDate,Double> result= new HashMap<LocalDate, Double>();
        for (LocalDate date = from.toLocalDate(); date.isBefore(to.toLocalDate()); date = date.plusDays(1))
        {
            result.put(date, 0.0);
        }
        for (Ride ride:rides) {
            Double distance=routingService.getRouteProperties(ride.getLocations().get(0).getDeparture().getLatitude(),ride.getLocations().get(0).getDeparture().getLongitude(),ride.getLocations().get(0).getDestination().getLatitude(),ride.getLocations().get(0).getDestination().getLongitude()).getDistance()/1000;
            result.put(ride.getStartTime().toLocalDate(), result.get(ride.getStartTime().toLocalDate())+distance);
        }
        ArrayList<LocalDate> sortedKeys
                = new ArrayList<LocalDate>(result.keySet());

        Collections.sort(sortedKeys);
        ArrayList<Double> sortedValues=new ArrayList<>();
        for (LocalDate key:sortedKeys) {
            sortedValues.add(result.get(key));
        }

        return new ReportOut(sortedKeys,sortedValues);


    }

    @Override
    public StatisticsOut getStatistics(Integer driverId){
        LocalDateTime fromTime=LocalDateTime.of(1753, Month.JANUARY,1,0, 0);
        LocalDateTime toTime=LocalDateTime.of(9998, Month.DECEMBER,31,0,0);
        List<Ride> rides = rideService.getRidesByDriverNonPageable(driverId,fromTime,toTime);
        double totalIncome=0;
        double totalRides=0;
        double totalDistance=0;
        double totalPassengers=0;
        double totalRating= 0;
        int ratingCount=0;
        double totalWorkHours=0;
        for (Ride ride:rides) {
            totalIncome+=ride.getTotalCost();
            totalRides+=1;
            totalPassengers+=ride.getPassengers().size();
            Location departure=ride.getLocations().get(0).getDeparture();
            Location destination=ride.getLocations().get(0).getDestination();
            totalDistance+=routingService.getRouteProperties(departure.getLatitude(),departure.getLongitude(),
                    destination.getLatitude(),destination.getLongitude()).getDistance();
        }

        for(RideReview review:reviewService.getRideReviewsByDriver(driverId)){
            ratingCount+=1;
            totalRating+=review.getRating();
        }
        DecimalFormat format = new DecimalFormat("0.#");
        double averageRating=Math.round(totalRating/ratingCount*10.0)/10.0;
        totalIncome=(Math.round(totalIncome*10.0)/10.0);
        totalDistance=Math.round(totalDistance/100.0)/10.0;

        for(WorkTime workTime: driverService.getDriverWorkingTimes(driverId,fromTime,toTime)){
            long diff =Math.abs(ChronoUnit.HOURS.between(workTime.getEndTime(),workTime.getStartTime()));
            totalWorkHours+=diff;
        }
        totalWorkHours=Math.round(totalWorkHours*10.0)/10.0;
        return  new StatisticsOut(totalIncome,totalRides,averageRating,totalWorkHours,totalDistance,totalPassengers);



    }


}
