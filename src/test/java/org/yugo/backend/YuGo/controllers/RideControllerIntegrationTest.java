package org.yugo.backend.YuGo.controllers;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.yugo.backend.YuGo.dto.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RideControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;
    private String adminToken;
    private String passengerToken;
    private String driverToken;

    @BeforeAll
    public void setup() {
        HttpHeaders headers = new HttpHeaders();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setOutputStreaming(false);
        restTemplate.getRestTemplate().setRequestFactory(requestFactory);

        HttpEntity<Login> adminRequest = new HttpEntity<>(new Login("marko.markovic@email.com", "Password123"), headers);
        ResponseEntity<TokenStateOut> adminResult = this.restTemplate.postForEntity("/api/user/login", adminRequest, TokenStateOut.class);
        adminToken = adminResult.getBody().getAccessToken();

        HttpEntity<Login> passengerRequest = new HttpEntity<>(new Login("pera.peric@email.com", "Password123"), headers);
        ResponseEntity<TokenStateOut> passengerResult = this.restTemplate.postForEntity("/api/user/login", passengerRequest, TokenStateOut.class);
        passengerToken = passengerResult.getBody().getAccessToken();

        HttpEntity<Login> driverRequest = new HttpEntity<>(new Login("perislav.peric@email.com", "Password123"), headers);
        ResponseEntity<TokenStateOut> driverResult = this.restTemplate.postForEntity("/api/user/login", driverRequest, TokenStateOut.class);
        driverToken = driverResult.getBody().getAccessToken();
    }

    private RideIn getRideExample(){
        LocationInOut departure = new LocationInOut("Bulevar oslobodjenja 46", 45.267136, 19.833549);
        LocationInOut destination = new LocationInOut("Bulevar oslobodjenja 46", 45.267136, 19.833549);
        PathInOut locations = new PathInOut();
        locations.setDeparture(departure);
        locations.setDestination(destination);
        UserSimplifiedOut passenger = new UserSimplifiedOut(1, "pera.peric@email.com");
        return new RideIn(List.of(locations), List.of(passenger),"STANDARD", true, true, "2023-01-11T17:45");
    }

    @Test
    @Order(1)
    @DisplayName("Should return unauthorized when making POST request to endpoint - /api/ride")
    public void ShouldReturnUnauthorizedForCreateRide() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer ");

        RideIn rideIn = getRideExample();
        HttpEntity<RideIn> createRideRequest = new HttpEntity<>(rideIn, headers);
        ResponseEntity<?> responseEntity = restTemplate.postForEntity("/api/ride", createRideRequest, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    @Order(2)
    @DisplayName("Should return unauthorized when making GET request to endpoint - /api/ride/passenger/{passengerId}/active")
    public void ShouldReturnUnauthorizedForGetActiveRideByPassenger() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer ");

        ResponseEntity<?> responseEntity = restTemplate.getForEntity("/api/ride/passenger/1/active", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    @Order(3)
    @DisplayName("Should return unauthorized when making GET request to endpoint - /api/ride/1")
    public void ShouldReturnUnauthorizedForGetRideById() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer ");

        ResponseEntity<?> responseEntity = restTemplate.getForEntity("/api/ride/1", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    @Order(4)
    @DisplayName("Should return forbidden when making POST request to endpoint - /api/ride")
    public void ShouldReturnForbiddenForCreateRide() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + adminToken);

        RideIn rideIn = getRideExample();
        HttpEntity<RideIn> createRideRequest = new HttpEntity<>(rideIn, headers);
        ResponseEntity<?> responseEntity = restTemplate.postForEntity("/api/ride", createRideRequest, String.class);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Test
    @Order(5)
    @DisplayName("Should return forbidden when making GET request to endpoint - /api/ride/passenger/{id}/active")
    public void ShouldReturnForbiddenForGetActiveRideByPassenger() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + driverToken);

        ResponseEntity<?> responseEntity = restTemplate.exchange("/api/ride/passenger/3/active",
                HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Test
    @Order(6)
    @DisplayName("Should create ride when making POST request to endpoint - /api/ride")
    public void ShouldReturnBadRequestForCreateRideBadParamsLocations() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);

        UserSimplifiedOut passenger = new UserSimplifiedOut(1, "pera.peric@email.com");
        RideIn rideIn = new RideIn(null, List.of(passenger),"STANDARD", true, true, "2023-01-11T17:45");

        HttpEntity<RideIn> createRideRequest = new HttpEntity<>(rideIn, headers);
        ResponseEntity<?> responseEntity = restTemplate.postForEntity("/api/ride", createRideRequest, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    @Order(7)
    @DisplayName("Should return bad request because of bad param passengers when making POST request to endpoint - /api/ride")
    public void ShouldReturnBadRequestForCreateRideBadParamsPassengers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);

        LocationInOut departure = new LocationInOut("Bulevar oslobodjenja 46", 45.267136, 19.833549);
        LocationInOut destination = new LocationInOut("Bulevar oslobodjenja 46", 45.267136, 19.833549);
        PathInOut locations = new PathInOut();
        locations.setDeparture(departure);
        locations.setDestination(destination);
        RideIn rideIn = new RideIn(List.of(locations), null,"STANDARD", true, true, "2023-01-11T17:45");

        HttpEntity<RideIn> createRideRequest = new HttpEntity<>(rideIn, headers);
        ResponseEntity<?> responseEntity = restTemplate.postForEntity("/api/ride", createRideRequest, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    @Order(8)
    @DisplayName("Should return bad request because of bad param vehicle type when making POST request to endpoint - /api/ride")
    public void ShouldReturnBadRequestForCreateRideBadParamsVehicleType() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);

        LocationInOut departure = new LocationInOut("Bulevar oslobodjenja 46", 45.267136, 19.833549);
        LocationInOut destination = new LocationInOut("Bulevar oslobodjenja 46", 45.267136, 19.833549);
        PathInOut locations = new PathInOut();
        locations.setDeparture(departure);
        locations.setDestination(destination);
        UserSimplifiedOut passenger = new UserSimplifiedOut(1, "pera.peric@email.com");
        RideIn rideIn = new RideIn(List.of(locations), List.of(passenger), null, true, true, "2023-01-11T17:45");

        HttpEntity<RideIn> createRideRequest = new HttpEntity<>(rideIn, headers);
        ResponseEntity<?> responseEntity = restTemplate.postForEntity("/api/ride", createRideRequest, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    @Order(9)
    @DisplayName("Should return bad request because of bad params when making GET request to endpoint - /api/ride/driver/{id}/active")
    public void ShouldReturnBadRequestForGetActiveRideByPassengerBadParams() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + driverToken);

        ResponseEntity<?> responseEntity = restTemplate.exchange("/api/ride/driver/asd/active",
                HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    @Order(10)
    @DisplayName("Should return bad request because of bad params when making GET request to endpoint - /api/ride/passenger/{id}/active")
    public void ShouldReturnBadRequestForGetActiveRideByDriverBadParams() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);

        ResponseEntity<?> responseEntity = restTemplate.exchange("/api/ride/passenger/asd/active",
                HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    @Order(11)
    @DisplayName("Should return bad request because of bad params when making GET request to endpoint - /api/ride/{id}")
    public void ShouldReturnBadRequestForRideDetailsBadParams() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);

        ResponseEntity<?> responseEntity = restTemplate.exchange("/api/ride/asd",
                HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    @Order(12)
    @DisplayName("Should create ride when making POST request to endpoint - /api/ride")
    public void ShouldCreateRide() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);

        RideIn rideIn = getRideExample();
        HttpEntity<RideIn> createRideRequest = new HttpEntity<>(rideIn, headers);
        ResponseEntity<RideDetailedOut> responseEntity = restTemplate.postForEntity("/api/ride", createRideRequest, RideDetailedOut.class);
        RideDetailedOut createdRide = responseEntity.getBody();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(createdRide.getId());
        assertEquals(rideIn.getVehicleType(), createdRide.getVehicleType());
        assertEquals(rideIn.isBabyTransport(), createdRide.isBabyTransport());
        assertEquals(rideIn.isPetTransport(), createdRide.isPetTransport());
        assertEquals(rideIn.getScheduledTime(), createdRide.getScheduledTime());
    }

    @Test
    @Order(13)
    @DisplayName("Should return bad request when making POST request to endpoint - /api/ride")
    public void ShouldReturnBadRequestForRideCreation() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);

        RideIn rideIn = getRideExample();
        HttpEntity<RideIn> createRideRequest = new HttpEntity<>(rideIn, headers);
        ResponseEntity<RideDetailedOut> responseEntity = restTemplate.postForEntity("/api/ride", createRideRequest, RideDetailedOut.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    @Order(14)
    @DisplayName("Should return active ride for driver when making GET request to endpoint - /api/ride/driver/{id}/active")
    public void ShouldReturnActiveRideForDriver() {
        ResponseEntity<RideDetailedOut> responseEntity = restTemplate.getForEntity("/api/ride/driver/7/active", RideDetailedOut.class);
        RideDetailedOut activeRide = responseEntity.getBody();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(1, activeRide.getId());
    }

    @Test
    @Order(15)
    @DisplayName("Should return not found when making GET request to endpoint - /api/ride/driver/{id}/active")
    public void ShouldReturnNotFoundForActiveRideForDriver() {
        ResponseEntity<RideDetailedOut> responseEntity = restTemplate.getForEntity("/api/ride/driver/6/active", RideDetailedOut.class);
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }

    @Test
    @Order(16)
    @DisplayName("Should return active ride for driver when making GET request to endpoint - /api/ride/passenger/{passengerId}/active")
    public void ShouldReturnActiveRideForPassenger() {
        HttpEntity<Login> passengerRequest = new HttpEntity<>(new Login("darko.darkovic@email.com", "Password123"));
        ResponseEntity<TokenStateOut> passengerResult = this.restTemplate.postForEntity("/api/user/login", passengerRequest, TokenStateOut.class);
        String currentPassengerToken = passengerResult.getBody().getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + currentPassengerToken);

        ResponseEntity<RideDetailedOut> responseEntity = restTemplate.exchange("/api/ride/passenger/3/active",
                HttpMethod.GET, new HttpEntity<>(headers), RideDetailedOut.class);
        RideDetailedOut activeRide = responseEntity.getBody();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(1, activeRide.getId());
    }

    @Test
    @Order(17)
    @DisplayName("Should return not found because of wrong token when making GET request to endpoint - /api/ride/passenger/{passengerId}/active")
    public void ShouldReturnNotFoundWrongTokenForActiveRideForPassenger() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);

        ResponseEntity<?> responseEntity = restTemplate.exchange("/api/ride/passenger/3/active",
                HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    @Order(18)
    @DisplayName("Should return not found when making GET request to endpoint - /api/ride/passenger/{passengerId}/active")
    public void ShouldReturnNotFoundForActiveRideForPassenger() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);

        ResponseEntity<?> responseEntity = restTemplate.exchange("/api/ride/passenger/1/active",
                HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    @Order(19)
    @DisplayName("Should return ride details when making GET request to endpoint - /api/ride/{id}")
    public void ShouldReturnRideDetails() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);

        ResponseEntity<RideDetailedOut> responseEntity = restTemplate.exchange("/api/ride/1",
                HttpMethod.GET, new HttpEntity<>(headers), RideDetailedOut.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    @Order(20)
    @DisplayName("Should return not found when making GET request to endpoint - /api/ride/{id}")
    public void ShouldReturnNotFoundForRideDetails() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);

        ResponseEntity<String> responseEntity = restTemplate.exchange("/api/ride/10",
                HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }
}