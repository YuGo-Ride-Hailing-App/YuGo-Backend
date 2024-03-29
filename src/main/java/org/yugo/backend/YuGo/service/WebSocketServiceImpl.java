package org.yugo.backend.YuGo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.yugo.backend.YuGo.dto.LiveMessageIn;
import org.yugo.backend.YuGo.dto.MessageOut;
import org.yugo.backend.YuGo.model.Message;
import org.yugo.backend.YuGo.model.User;

@Service
public class WebSocketServiceImpl implements WebSocketService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public WebSocketServiceImpl(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public void notifyAdminAboutLiveChatMessage(LiveMessageIn liveMessageIn){
        simpMessagingTemplate.convertAndSend("/live-chat-topic/admin", liveMessageIn);
    }

    @Override
    public void notifyUserAboutLiveChatResponse(Integer senderId, LiveMessageIn liveMessageIn){
        simpMessagingTemplate.convertAndSend("/live-chat-topic/" + senderId, liveMessageIn);
    }

    @Override
    public void notifyUserAboutMessage(Integer receiverId, Message message){
        MessageOut messageOut = new MessageOut(message);
        simpMessagingTemplate.convertAndSend("/message-topic/" + receiverId, messageOut);
    }

    private class PanicDTO{
        public Integer panicId;
    }

    @Override
    public void notifyAdminsAboutPanic(Integer panicId){
        PanicDTO output = new PanicDTO();
        output.panicId = panicId;
        System.out.println("============================");
        System.out.println("Admin je obavesten");
        System.out.println("============================");
        simpMessagingTemplate.convertAndSend("/ride-topic/notify-admin-panic", output);
    }

    private class RideDTO{
        public Integer rideID;
    }

    @Override
    public void sendRideRequestToDriver(Integer driverID, Integer rideID){
        RideDTO output = new RideDTO();
        output.rideID = rideID;
        System.out.println("============================");
        System.out.println("Vozac je obavesten");
        System.out.println("============================");
        simpMessagingTemplate.convertAndSend("/ride-topic/driver-request/" + driverID, output);
    }
    @Override
    public void notifyPassengerAboutRide(Integer rideID, Integer passengerID){
        RideDTO output = new RideDTO();
        output.rideID = rideID;
        System.out.println("============================");
        System.out.println("Putnik je obavesten");
        System.out.println("============================");
        simpMessagingTemplate.convertAndSend("/ride-topic/notify-passenger/" + passengerID, output);
    }
    @Override
    public void notifyPassengerAboutRideEnd(Integer passengerID,Integer rideID){
        RideDTO output = new RideDTO();
        output.rideID = rideID;
        System.out.println("============================");
        System.out.println("Putnik je obavesten");
        System.out.println("============================");
        simpMessagingTemplate.convertAndSend("/ride-topic/notify-passenger-end-ride/" + passengerID,output);
    }

    @Override
    public void notifyPassengerAboutRideStart(Integer passengerID){
        System.out.println("============================");
        System.out.println("Putnik je obavesten");
        System.out.println("============================");
        simpMessagingTemplate.convertAndSend("/ride-topic/notify-passenger-start-ride/" + passengerID,"");
    }
    private class CoordinatesDTO{
        public double longitude;
        public double latitude;
    }
    @Override
    public void notifyPassengerAboutVehicleLocation(Integer passengerID, double longitude, double latitude){
        CoordinatesDTO coordinates = new CoordinatesDTO();
        coordinates.latitude = latitude;
        coordinates.longitude = longitude;
        simpMessagingTemplate.convertAndSend("/ride-topic/notify-passenger-vehicle-location/" + passengerID, coordinates);
    }
    @Override
    public void notifyAddedPassenger(Integer passengerID, Integer rideID){
        RideDTO ride = new RideDTO();
        ride.rideID = rideID;
        simpMessagingTemplate.convertAndSend("/ride-topic/notify-added-passenger/" + passengerID, ride);
    }
    @Override
    public void notifyPassengerThatVehicleHasArrived(Integer passengerID, Integer rideID){
        RideDTO ride = new RideDTO();
        ride.rideID = rideID;
        simpMessagingTemplate.convertAndSend("/ride-topic/notify-passenger-vehicle-arrival/" + passengerID, ride);
    }
}
