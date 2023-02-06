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
import org.yugo.backend.YuGo.model.RideStatus;

import java.util.*;

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
    private String passengerToken2;
    private String driverToken;
    private String driverToken2;
    private final Integer passengerID = 1;
    private String driver200Token;
    Integer driver200ID = 8;
    private String driver201Token;
    Integer driver201ID = 9;
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
        HttpEntity<Login> passengerRequest2 = new HttpEntity<>(new Login("darko.darkovic@email.com", "Password123"), headers);
        ResponseEntity<TokenStateOut> passengerResult2 = this.restTemplate.postForEntity("/api/user/login", passengerRequest2, TokenStateOut.class);
        passengerToken2 = passengerResult2.getBody().getAccessToken();

        HttpEntity<Login> driverRequest = new HttpEntity<>(new Login("perislav.peric@email.com", "Password123"), headers);
        ResponseEntity<TokenStateOut> driverResult = this.restTemplate.postForEntity("/api/user/login", driverRequest, TokenStateOut.class);
        driverToken = driverResult.getBody().getAccessToken();

        HttpEntity<Login> driverRequest2 = new HttpEntity<>(new Login("nikola.nikolic@email.com", "Password123"), headers);
        ResponseEntity<TokenStateOut> driverResult2 = this.restTemplate.postForEntity("/api/user/login", driverRequest2, TokenStateOut.class);
        driverToken2= driverResult2.getBody().getAccessToken();

        HttpEntity<Login> driver200TokenRequest = new HttpEntity<>(new Login("velibor.stojkovic@email.com", "Password123"), headers);
        ResponseEntity<TokenStateOut> driver200TokenResult = this.restTemplate.postForEntity("/api/user/login", driver200TokenRequest, TokenStateOut.class);
        driver200Token = driver200TokenResult.getBody().getAccessToken();

        HttpEntity<Login> driver201TokenRequest = new HttpEntity<>(new Login("velibor.stojkovic@email.com", "Password123"), headers);
        ResponseEntity<TokenStateOut> driver201TokenResult = this.restTemplate.postForEntity("/api/user/login", driver201TokenRequest, TokenStateOut.class);
        driver201Token = driver201TokenResult.getBody().getAccessToken();

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


    @Test
    @Order(21)
    @DisplayName("Shouldnt withdraw ride when making Put request to endpoint - /api/ride/{id}/withdraw 401")
    public void ShouldCancelRideNoAuthorization() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer afeijoafji");
        HttpEntity<RideIn> cancelRide = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","2");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/withdraw"), HttpMethod.PUT,cancelRide, String.class, param);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    @Order(22)
    @DisplayName("Shouldnt withdraw ride when making Put request to endpoint - /api/ride/{id}/withdraw 403")
    public void ShouldCancelRideForbidden() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ driverToken);
        HttpEntity<RideIn> cancelRide = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","2");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/withdraw"), HttpMethod.PUT,cancelRide, String.class, param);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Test
    @Order(23)
    @DisplayName("Shouldnt withdraw ride when making Put request to endpoint - /api/ride/{id}/withdraw 404")
    public void ShouldCancelRideNotFound() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ passengerToken2);
        HttpEntity<RideIn> cancelRide = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","14");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/withdraw"), HttpMethod.PUT,cancelRide, String.class, param);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    @Order(24)
    @DisplayName("Shouldnt withdraw ride when making Put request to endpoint - /api/ride/{id}/withdraw 404- Passenger is not in ride")
    public void ShouldCancelRideNotFound2() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ passengerToken);
        HttpEntity<RideIn> cancelRide = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","2");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/withdraw"), HttpMethod.PUT,cancelRide, String.class, param);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    @Order(25)
    @DisplayName("Should withdrawRide ride when making Put request to endpoint - /api/ride/{id}/withdraw")
    public void ShouldCancelRide() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken2);
        HttpEntity<RideIn> cancelRide = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","2");
        ResponseEntity<RideDetailedOut> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/withdraw"), HttpMethod.PUT,cancelRide, RideDetailedOut.class, param);
        RideDetailedOut canceledRide = responseEntity.getBody();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(canceledRide.getId());
        assertEquals("CANCELED",canceledRide.getStatus());
    }

    @Test
    @Order(26)
    @DisplayName("Shouldnt withdraw ride when making Put request to endpoint - /api/ride/{id}/withdraw 400")
    public void ShouldCancelRideBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ passengerToken2);
        HttpEntity<RideIn> cancelRide = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","2");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/withdraw"), HttpMethod.PUT,cancelRide, String.class, param);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    @Order(27)
    @DisplayName("Shouldnt withdraw ride when making Put request to endpoint - /api/ride/{id}/withdraw 400")
    public void ShouldCancelRideBadRequest2() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ passengerToken2);
        HttpEntity<RideIn> cancelRide = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","asd");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/withdraw"), HttpMethod.PUT,cancelRide, String.class, param);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }


    @Test
    @Order(28)
    @DisplayName("Shouldnt accept ride when making Put request to endpoint - /api/ride/{id}/accept 401")
    public void ShouldAcceptRideNoAuthorization() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer afeijoafji");
        HttpEntity<RideIn> acceptRideRequest = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","3");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/accept"), HttpMethod.PUT,acceptRideRequest, String.class, param);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    @Order(29)
    @DisplayName("Shouldnt accept ride when making Put request to endpoint - /api/ride/{id}/accept 403")
    public void ShouldAcceptRideForbidden() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ passengerToken2);
        HttpEntity<RideIn> acceptRideRequest = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","3");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/accept"), HttpMethod.PUT,acceptRideRequest, String.class, param);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Test
    @Order(30)
    @DisplayName("Shouldnt accept ride when making Put request to endpoint - /api/ride/{id}/accept 404")
    public void ShouldAcceptRideNotFound() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ driverToken);
        HttpEntity<RideIn> acceptRideRequest = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","14");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/accept"), HttpMethod.PUT,acceptRideRequest, String.class, param);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    @Order(31)
    @DisplayName("Should accept ride when making Put request to endpoint - /api/ride/{id}/accept")
    public void ShouldAcceptRide() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + driverToken);
        HttpEntity<RideIn> acceptRideRequest = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","4");
        ResponseEntity<RideDetailedOut> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/accept"), HttpMethod.PUT,acceptRideRequest, RideDetailedOut.class, param);
        RideDetailedOut acceptedRide = responseEntity.getBody();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(acceptedRide.getId());
        assertEquals("ACCEPTED",acceptedRide.getStatus());
    }

    @Test
    @Order(32)
    @DisplayName("Shouldnt accept ride when making Put request to endpoint - /api/ride/{id}/accept 400")
    public void ShouldAcceptedRideBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ driverToken);
        HttpEntity<RideIn> acceptRideRequest = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","4");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/accept"), HttpMethod.PUT, acceptRideRequest, String.class, param);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    @Order(33)
    @DisplayName("Shouldnt accept ride when making Put request to endpoint - /api/ride/{id}/accept 400")
    public void ShouldAcceptedRideBadRequest2() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ driverToken);
        HttpEntity<RideIn> acceptRideRequest = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","dsljk");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/accept"), HttpMethod.PUT, acceptRideRequest, String.class, param);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    @Order(34)
    @DisplayName("Shouldnt start ride when making Put request to endpoint - /api/ride/{id}/start 401")
    public void ShouldStartRideNoAuthorization() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer afeijoafji");
        HttpEntity<RideIn> startRideRequest = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","4");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/start"), HttpMethod.PUT, startRideRequest, String.class, param);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    @Order(35)
    @DisplayName("Shouldnt start ride when making Put request to endpoint - /api/ride/{id}/start 403")
    public void ShouldStartRideForbidden() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ passengerToken2);
        HttpEntity<RideIn> startRideRequest = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","4");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/start"), HttpMethod.PUT, startRideRequest, String.class, param);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Test
    @Order(36)
    @DisplayName("Shouldnt start ride when making Put request to endpoint - /api/ride/{id}/start 404")
    public void ShouldStartRideNotFound() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ driverToken);
        HttpEntity<RideIn> startRideRequest = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","14");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/start"), HttpMethod.PUT, startRideRequest, String.class, param);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    @Order(37)
    @DisplayName("Shouldnt start ride when making Put request to endpoint - /api/ride/{id}/start 404- Passenger is not in ride")
    public void ShouldStartRideNotFound2() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ driverToken2);
        HttpEntity<RideIn> startRideRequest = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","4");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/start"), HttpMethod.PUT, startRideRequest, String.class, param);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    @Order(38)
    @DisplayName("Should start ride when making Put request to endpoint - /api/ride/{id}/start")
    public void ShouldStartRide() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + driverToken);
        HttpEntity<RideIn> startRideRequest = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","4");
        ResponseEntity<RideDetailedOut> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/start"), HttpMethod.PUT, startRideRequest, RideDetailedOut.class, param);
        RideDetailedOut startedRide = responseEntity.getBody();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(startedRide.getId());
        assertEquals("ACTIVE",startedRide.getStatus());
    }

    @Test
    @Order(39)
    @DisplayName("Shouldnt start ride when making Put request to endpoint - /api/ride/{id}/start 400")
    public void ShouldStartRideBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ driverToken);
        HttpEntity<RideIn> startRideRequest = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","4");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/start"), HttpMethod.PUT, startRideRequest, String.class, param);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    @Order(40)
    @DisplayName("Shouldnt start ride when making Put request to endpoint - /api/ride/{id}/start 400")
    public void ShouldStartRideBadRequest2() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ driverToken);
        HttpEntity<RideIn> startRideRequest = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","dsad");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/start"), HttpMethod.PUT, startRideRequest, String.class, param);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    @Order(41)
    @DisplayName("Shouldnt add panic ride when making Put request to endpoint - /api/ride/{id}/panic 401")
    public void ShouldAddPanicRideNoAuthorization() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer afeijoafji");
        HttpEntity<RideIn> addPanicRequest = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","4");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/panic"), HttpMethod.PUT, addPanicRequest, String.class, param);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    @Order(42)
    @DisplayName("Shouldnt add panic ride when making Put request to endpoint - /api/ride/{id}/panic 403")
    public void ShouldAddPanicRideForbidden() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ adminToken);
        ReasonIn reason=new ReasonIn();
        reason.setReason("Neki reason");
        HttpEntity<ReasonIn> addPanicRequest = new HttpEntity<>(reason,headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","4");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/panic"), HttpMethod.PUT, addPanicRequest, String.class, param);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Test
    @Order(43)
    @DisplayName("Shouldnt add panic ride when making Put request to endpoint - /api/ride/{id}/panic 404")
    public void ShouldAddPanicRideNotFound() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ driverToken);
        ReasonIn reason=new ReasonIn();
        reason.setReason("Neki reason");
        HttpEntity<ReasonIn> addPanicRequest = new HttpEntity<>(reason,headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","14");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/panic"), HttpMethod.PUT, addPanicRequest, String.class, param);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    @Order(44)
    @DisplayName("Shouldnt add panic ride when making Put request to endpoint - /api/ride/{id}/panic 404- driver is not in ride")
    public void ShouldAddPanicRideNotFound2() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ driverToken2);
        ReasonIn reason=new ReasonIn();
        reason.setReason("Neki reason");
        HttpEntity<ReasonIn> addPanicRequest = new HttpEntity<>(reason,headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","4");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/panic"), HttpMethod.PUT, addPanicRequest, String.class, param);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    @Order(45)
    @DisplayName("Should add panic ride when making Put request to endpoint - /api/ride/{id}/panic")
    public void ShouldAddPanicRide() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + driverToken);
        ReasonIn reason=new ReasonIn();
        reason.setReason("Neki reason");
        HttpEntity<ReasonIn> addPanicRequest = new HttpEntity<>(reason,headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","4");
        ResponseEntity<RideDetailedOut> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/panic"), HttpMethod.PUT, addPanicRequest, RideDetailedOut.class, param);
        RideDetailedOut addedRide = responseEntity.getBody();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(addedRide.getId());
    }

    @Test
    @Order(46)
    @DisplayName("Should add panic ride when making Put request to endpoint - /api/ride/{id}/panic")
    public void ShouldAddPanicRideBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + driverToken);
        HttpEntity<ReasonIn> addPanicRequest = new HttpEntity<>(null,headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","4");
        ResponseEntity<RideDetailedOut> responseEntity = restTemplate.exchange(String.format("/api/ride/{id}/panic"), HttpMethod.PUT, addPanicRequest, RideDetailedOut.class, param);
        RideDetailedOut addedRide = responseEntity.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    @Order(47)
    @DisplayName("Shouldnt delete favorite path  when making delete request to endpoint - /api/ride/{id}/favorites 401")
    public void ShouldDeleteFavoriteRideNoAuthorization() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer afeijoafji");
        HttpEntity<RideIn> deletedRideRequest = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","1");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/favorites/{id}"), HttpMethod.DELETE, deletedRideRequest, String.class, param);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    @Order(48)
    @DisplayName("Shouldnt delete favorite path  when making delete request to endpoint - /api/ride/{id}/favorites 403")
    public void ShouldDeleteFavoriteForbidden() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ adminToken);
        HttpEntity<ReasonIn> deletedRideRequest = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","1");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/favorites/{id}"), HttpMethod.DELETE, deletedRideRequest, String.class, param);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Test
    @Order(49)
    @DisplayName("Shouldnt delete favorite path  when making delete request to endpoint - /api/ride/{id}/favorites 404")
    public void ShouldDeleteFavoriteNotFound() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ passengerToken);
        HttpEntity<ReasonIn> deletedRideRequest = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","14");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/favorites/{id}"), HttpMethod.DELETE, deletedRideRequest, String.class, param);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    @Order(50)
    @DisplayName("Shouldnt delete favorite path  when making delete request to endpoint - /api/ride/{id}/favorite 404- not his ride")
    public void ShouldDeleteFavoriteNotFound2() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ passengerToken2);
        HttpEntity<ReasonIn> deletedRideRequest = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","4");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/favorites/{id}"), HttpMethod.DELETE, deletedRideRequest, String.class, param);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    @Order(51)
    @DisplayName("Shouldnt delete favorite path  when making delete request to endpoint - /api/ride/{id}/favorite 400")
    public void ShouldDeleteFavoriteBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ passengerToken2);
        HttpEntity<ReasonIn> deletedRideRequest = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","adsg");
        ResponseEntity<?> responseEntity = restTemplate.exchange(String.format("/api/ride/favorites/{id}"), HttpMethod.DELETE, deletedRideRequest, String.class, param);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    private RideDetailedOut driver200Ride;
    private final Integer driver201RideID = 3;
    @Test
    @DisplayName("Driver can end ride if ride status is FINISHED")
    @Order(200)
    @Tag("vlada")
    public void shouldEndRideForExistingRide(){
        ResponseEntity<RideDetailedOut> activeRideRequest = restTemplate.getForEntity("/api/ride/driver/" + driver200ID + "/active", RideDetailedOut.class);
        RideDetailedOut activeRide = activeRideRequest.getBody();
        if(activeRide == null){
            fail("This test requires active ride to run properly");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + driver200Token);
        HttpEntity<RideIn> endRideRequest = new HttpEntity<>(null, headers);

        ResponseEntity<RideDetailedOut> endRideResponse = restTemplate.exchange("/api/ride/" + activeRide.getId() + "/end", HttpMethod.PUT, endRideRequest, RideDetailedOut.class);
        RideDetailedOut responseRide = endRideResponse.getBody();
        driver200Ride = responseRide;

        if(responseRide == null){
            fail();
        }
        assertEquals(HttpStatus.OK, endRideResponse.getStatusCode());
        assertEquals(activeRide.getId(), responseRide.getId());
        assertEquals(RideStatus.FINISHED.toString(), responseRide.getStatus());
    }
    @Test
    @DisplayName("End ride returns bad request when ending ride that is not in status FINISHED")
    @Order(201)
    @Tag("vlada")
    public void shouldReturnBadRequestForFinishedRide(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + driver200Token);
        HttpEntity<RideIn> endRideRequest = new HttpEntity<>(null, headers);

        Map<String, Integer> params = new HashMap<>();
        params.put("id", driver200Ride.getId());
        ResponseEntity<RideDetailedOut> endRideResponse = restTemplate.exchange("/api/ride/{id}/end", HttpMethod.PUT, endRideRequest, RideDetailedOut.class, params);

        assertEquals(HttpStatus.BAD_REQUEST, endRideResponse.getStatusCode());
    }

    @Test
    @DisplayName("End ride returns not found when ride does not exist")
    @Order(202)
    @Tag("vlada")
    public void shouldReturnNotFoundForNonexistentRide(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + driver200Token);
        HttpEntity<RideIn> endRideRequest = new HttpEntity<>(null, headers);

        Map<String, Integer> params = new HashMap<>();
        params.put("id", 99999999);
        ResponseEntity<String> endRideResponse = restTemplate.exchange("/api/ride/{id}/end", HttpMethod.PUT, endRideRequest, String.class, params);

        assertEquals(HttpStatus.NOT_FOUND, endRideResponse.getStatusCode());
    }

    @Test
    @Order(203)
    @DisplayName("End ride returns unauthorized if sender is unauthorized")
    @Tag("vlada")
    public void endRideShouldReturnUnauthorizedIfSenderIsUnauthorized(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RideIn> endRideRequest = new HttpEntity<>(null, headers);

        Map<String, Integer> params = new HashMap<>();
        params.put("id", 99999);
        ResponseEntity<String> endRideResponse = restTemplate.exchange("/api/ride/{id}/end", HttpMethod.PUT, endRideRequest, String.class, params);

        assertEquals(HttpStatus.UNAUTHORIZED, endRideResponse.getStatusCode());
    }
    @Test
    @Order(204)
    @DisplayName("End ride returns forbidden if sender is not a driver")
    @Tag("vlada")
    public void endRideShouldReturnForbiddenIfSenderIsNotDriver(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);
        HttpEntity<RideIn> endRideRequest = new HttpEntity<>(null, headers);

        Map<String, Integer> params = new HashMap<>();
        params.put("id", 99999);
        ResponseEntity<String> endRideResponse = restTemplate.exchange("/api/ride/{id}/end", HttpMethod.PUT, endRideRequest, String.class, params);

        assertEquals(HttpStatus.FORBIDDEN, endRideResponse.getStatusCode());
    }

    @Test
    @Order(210)
    @DisplayName("Driver can cancel the ride if ride status is PENDING")
    @Tag("vlada")
    public void shouldCancelRideThatHasStatusPending(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + driver201Token);

        ReasonIn reason = new ReasonIn();
        reason.setReason("Default message");

        HttpEntity<ReasonIn> cancelRideRequest = new HttpEntity<>(reason, headers);

        Map<String, Integer> params = new HashMap<>();
        params.put("id", driver201RideID);
        ResponseEntity<RideDetailedOut> cancelRideResponse = restTemplate.exchange("/api/ride/{id}/cancel", HttpMethod.PUT, cancelRideRequest, RideDetailedOut.class, params);
        RideDetailedOut responseRide = cancelRideResponse.getBody();

        if(responseRide == null){
            fail();
        }
        assertEquals(HttpStatus.OK, cancelRideResponse.getStatusCode());
        assertEquals(driver201RideID, responseRide.getId());
        assertEquals(RideStatus.REJECTED.toString(), responseRide.getStatus());
    }

    @Test
    @Order(211)
    @DisplayName("Cancel ride returns bad request if ride status is not PENDING")
    @Tag("vlada")
    public void cancelRideShouldReturnBadRequestIfRideStatusIsNotPending(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + driver201Token);

        ReasonIn reason = new ReasonIn();
        reason.setReason("Default message");

        HttpEntity<ReasonIn> cancelRideRequest = new HttpEntity<>(reason, headers);

        Map<String, Integer> params = new HashMap<>();
        params.put("id", driver201RideID);
        ResponseEntity<String> cancelRideResponse = restTemplate.exchange("/api/ride/{id}/cancel", HttpMethod.PUT, cancelRideRequest, String.class, params);

        assertEquals(HttpStatus.BAD_REQUEST, cancelRideResponse.getStatusCode());
    }

    @Test
    @Order(212)
    @DisplayName("Cancel ride returns not found if ride does not exist")
    @Tag("vlada")
    public void cancelRideShouldReturnNotFoundtIfRideDoesNotExist(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + driver201Token);

        ReasonIn reason = new ReasonIn();
        reason.setReason("Default message");

        HttpEntity<ReasonIn> cancelRideRequest = new HttpEntity<>(reason, headers);

        Map<String, Integer> params = new HashMap<>();
        params.put("id", 9999999);
        ResponseEntity<String> cancelRideResponse = restTemplate.exchange("/api/ride/{id}/cancel", HttpMethod.PUT, cancelRideRequest, String.class, params);

        assertEquals(HttpStatus.NOT_FOUND, cancelRideResponse.getStatusCode());
    }

    @Test
    @Order(213)
    @DisplayName("Cancel ride returns unauthorized if sender is unauthorized")
    @Tag("vlada")
    public void cancelRideShouldReturnUnauthorizedIfSenderIsUnauthorized(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ReasonIn reason = new ReasonIn();
        reason.setReason("Default message");

        HttpEntity<ReasonIn> cancelRideRequest = new HttpEntity<>(reason, headers);

        Map<String, Integer> params = new HashMap<>();
        params.put("id", 9999999);
        ResponseEntity<String> cancelRideResponse = restTemplate.exchange("/api/ride/{id}/cancel", HttpMethod.PUT, cancelRideRequest, String.class, params);

        assertEquals(HttpStatus.UNAUTHORIZED, cancelRideResponse.getStatusCode());
    }

    @Test
    @Order(214)
    @DisplayName("Cancel ride returns forbidden if sender is not a driver")
    @Tag("vlada")
    public void cancelRideShouldReturnForbiddenIfSenderIsNotDriver(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);

        ReasonIn reason = new ReasonIn();
        reason.setReason("Default message");

        HttpEntity<ReasonIn> cancelRideRequest = new HttpEntity<>(reason, headers);

        Map<String, Integer> params = new HashMap<>();
        params.put("id", 9999999);
        ResponseEntity<String> cancelRideResponse = restTemplate.exchange("/api/ride/{id}/cancel", HttpMethod.PUT, cancelRideRequest, String.class, params);

        assertEquals(HttpStatus.FORBIDDEN, cancelRideResponse.getStatusCode());
    }

    private final List<FavoritePathOut> passengerFavoritePaths = new ArrayList<>();
    @Test
    @Order(220)
    @DisplayName("Creating favorite ride returns favorite ride")
    @Tag("vlada")
    public void createFavoriteRideReturnsFavoriteRide(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);

        LocationInOut departure = new LocationInOut();
        departure.setLatitude(45);
        departure.setLongitude(45);
        departure.setAddress("Default address");
        LocationInOut destination = new LocationInOut();
        destination.setLatitude(46);
        destination.setLongitude(46);
        destination.setAddress("Default address");


        PathInOut path = new PathInOut();
        path.setDestination(destination);
        path.setDeparture(departure);
        List<PathInOut> paths = List.of(path);

        UserSimplifiedOut passenger = new UserSimplifiedOut();
        passenger.setId(passengerID);
        passenger.setEmail("pera.peric@email.com");

        FavoritePathIn favoritePath = new FavoritePathIn();
        favoritePath.setFavoriteName("Default name");
        favoritePath.setBabyTransport(true);
        favoritePath.setPetTransport(false);
        favoritePath.setVehicleType("STANDARD");
        favoritePath.setLocations(paths);
        favoritePath.setPassengers(List.of(passenger));

        HttpEntity<FavoritePathIn> createFavoriteRideRequest = new HttpEntity<>(favoritePath, headers);


        ResponseEntity<FavoritePathOut> createFavRideResponse = restTemplate.exchange("/api/ride/favorites", HttpMethod.POST, createFavoriteRideRequest, FavoritePathOut.class);
        FavoritePathOut favoritePathResponse = createFavRideResponse.getBody();

        if(favoritePathResponse == null){
            fail();
        }
        passengerFavoritePaths.add(favoritePathResponse);
        assertEquals(HttpStatus.OK, createFavRideResponse.getStatusCode());
        assertNotNull(favoritePathResponse.getId());
        assertEquals(favoritePath.getFavoriteName(), favoritePathResponse.getFavoriteName());
        assertEquals(favoritePath.getBabyTransport(), favoritePathResponse.getBabyTransport());
        assertEquals(favoritePath.getPetTransport(), favoritePathResponse.getPetTransport());
    }
    @Test
    @Order(221)
    @DisplayName("Creating favorite ride returns bad request if favorite ride name is longer than 50 characters")
    @Tag("vlada")
    public void createFavoriteRideReturnsBadRequestIfNameIsLongerThan50Chars(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);

        LocationInOut departure = new LocationInOut();
        departure.setLatitude(45);
        departure.setLongitude(45);
        departure.setAddress("Default address");
        LocationInOut destination = new LocationInOut();
        destination.setLatitude(46);
        destination.setLongitude(46);
        destination.setAddress("Default address");


        PathInOut path = new PathInOut();
        path.setDestination(destination);
        path.setDeparture(departure);
        List<PathInOut> paths = List.of(path);

        UserSimplifiedOut passenger = new UserSimplifiedOut();
        passenger.setId(passengerID);
        passenger.setEmail("pera.peric@email.com");

        FavoritePathIn favoritePath = new FavoritePathIn();
        favoritePath.setFavoriteName("Default naaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaame");
        favoritePath.setBabyTransport(true);
        favoritePath.setPetTransport(false);
        favoritePath.setVehicleType("STANDARD");
        favoritePath.setLocations(paths);
        favoritePath.setPassengers(List.of(passenger));

        HttpEntity<FavoritePathIn> createFavoriteRideRequest = new HttpEntity<>(favoritePath, headers);


        ResponseEntity<String> createFavRideResponse = restTemplate.exchange("/api/ride/favorites", HttpMethod.POST, createFavoriteRideRequest, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, createFavRideResponse.getStatusCode());
    }
    @Test
    @Order(222)
    @DisplayName("Creating favorite ride returns bad request if locations are null")
    @Tag("vlada")
    public void createFavoriteRideReturnsBadRequestIfLocationsAreNull(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);

        UserSimplifiedOut passenger = new UserSimplifiedOut();
        passenger.setId(passengerID);
        passenger.setEmail("pera.peric@email.com");

        FavoritePathIn favoritePath = new FavoritePathIn();
        favoritePath.setFavoriteName("Default name");
        favoritePath.setBabyTransport(true);
        favoritePath.setPetTransport(false);
        favoritePath.setVehicleType("STANDARD");
        favoritePath.setLocations(null);
        favoritePath.setPassengers(List.of(passenger));

        HttpEntity<FavoritePathIn> createFavoriteRideRequest = new HttpEntity<>(favoritePath, headers);


        ResponseEntity<String> createFavRideResponse = restTemplate.exchange("/api/ride/favorites", HttpMethod.POST, createFavoriteRideRequest, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, createFavRideResponse.getStatusCode());
    }

    @Test
    @Order(223)
    @DisplayName("Creating favorite ride returns bad request if passengers are null")
    @Tag("vlada")
    public void createFavoriteRideReturnsBadRequestIfPassengersAreNull(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);

        LocationInOut departure = new LocationInOut();
        departure.setLatitude(45);
        departure.setLongitude(45);
        departure.setAddress("Default address");
        LocationInOut destination = new LocationInOut();
        destination.setLatitude(46);
        destination.setLongitude(46);
        destination.setAddress("Default address");


        PathInOut path = new PathInOut();
        path.setDestination(destination);
        path.setDeparture(departure);
        List<PathInOut> paths = List.of(path);

        FavoritePathIn favoritePath = new FavoritePathIn();
        favoritePath.setFavoriteName("Default name");
        favoritePath.setBabyTransport(true);
        favoritePath.setPetTransport(false);
        favoritePath.setVehicleType("STANDARD");
        favoritePath.setLocations(paths);
        favoritePath.setPassengers(null);

        HttpEntity<FavoritePathIn> createFavoriteRideRequest = new HttpEntity<>(favoritePath, headers);


        ResponseEntity<String> createFavRideResponse = restTemplate.exchange("/api/ride/favorites", HttpMethod.POST, createFavoriteRideRequest, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, createFavRideResponse.getStatusCode());
    }

    @Test
    @Order(224)
    @DisplayName("Creating favorite ride returns bad request if vehicle type is null")
    @Tag("vlada")
    public void createFavoriteRideReturnsBadRequestIfVehicleTypeIsNull(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);

        LocationInOut departure = new LocationInOut();
        departure.setLatitude(45);
        departure.setLongitude(45);
        departure.setAddress("Default address");
        LocationInOut destination = new LocationInOut();
        destination.setLatitude(46);
        destination.setLongitude(46);
        destination.setAddress("Default address");


        PathInOut path = new PathInOut();
        path.setDestination(destination);
        path.setDeparture(departure);
        List<PathInOut> paths = List.of(path);

        UserSimplifiedOut passenger = new UserSimplifiedOut();
        passenger.setId(passengerID);
        passenger.setEmail("pera.peric@email.com");

        FavoritePathIn favoritePath = new FavoritePathIn();
        favoritePath.setFavoriteName("Default name");
        favoritePath.setBabyTransport(true);
        favoritePath.setPetTransport(false);
        favoritePath.setVehicleType(null);
        favoritePath.setLocations(paths);
        favoritePath.setPassengers(List.of(passenger));

        HttpEntity<FavoritePathIn> createFavoriteRideRequest = new HttpEntity<>(favoritePath, headers);


        ResponseEntity<String> createFavRideResponse = restTemplate.exchange("/api/ride/favorites", HttpMethod.POST, createFavoriteRideRequest, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, createFavRideResponse.getStatusCode());
    }

    @Test
    @Order(225)
    @DisplayName("Creating favorite ride returns bad request if vehicle contains more than 30 characters")
    @Tag("vlada")
    public void createFavoriteRideReturnsBadRequestIfVehicleTypeContainsMoreThan30Chars(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);

        LocationInOut departure = new LocationInOut();
        departure.setLatitude(45);
        departure.setLongitude(45);
        departure.setAddress("Default address");
        LocationInOut destination = new LocationInOut();
        destination.setLatitude(46);
        destination.setLongitude(46);
        destination.setAddress("Default address");


        PathInOut path = new PathInOut();
        path.setDestination(destination);
        path.setDeparture(departure);
        List<PathInOut> paths = List.of(path);

        UserSimplifiedOut passenger = new UserSimplifiedOut();
        passenger.setId(passengerID);
        passenger.setEmail("pera.peric@email.com");

        FavoritePathIn favoritePath = new FavoritePathIn();
        favoritePath.setFavoriteName("Default name");
        favoritePath.setBabyTransport(true);
        favoritePath.setPetTransport(false);
        favoritePath.setVehicleType("STAAAAAAAAAAAAAAAAAAAAAAAAAAANDARD");
        favoritePath.setLocations(paths);
        favoritePath.setPassengers(List.of(passenger));

        HttpEntity<FavoritePathIn> createFavoriteRideRequest = new HttpEntity<>(favoritePath, headers);


        ResponseEntity<String> createFavRideResponse = restTemplate.exchange("/api/ride/favorites", HttpMethod.POST, createFavoriteRideRequest, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, createFavRideResponse.getStatusCode());
    }
    private void createFavoriteRides(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);
        for(int i = 0; i < 10; i++){
            LocationInOut departure = new LocationInOut();
            departure.setLatitude(45);
            departure.setLongitude(45);
            departure.setAddress("Default address");
            LocationInOut destination = new LocationInOut();
            destination.setLatitude(46);
            destination.setLongitude(46);
            destination.setAddress("Default address");

            PathInOut path = new PathInOut();
            path.setDestination(destination);
            path.setDeparture(departure);
            List<PathInOut> paths = List.of(path);

            UserSimplifiedOut passenger = new UserSimplifiedOut();
            passenger.setId(passengerID);
            passenger.setEmail("pera.peric@email.com");

            FavoritePathIn favoritePath = new FavoritePathIn();
            favoritePath.setFavoriteName("Default name");
            favoritePath.setBabyTransport(true);
            favoritePath.setPetTransport(false);
            favoritePath.setVehicleType("STANDARD");
            favoritePath.setLocations(paths);
            favoritePath.setPassengers(List.of(passenger));

            HttpEntity<FavoritePathIn> createFavoriteRideRequest = new HttpEntity<>(favoritePath, headers);

            ResponseEntity<FavoritePathOut> createFavRideResponse = restTemplate.exchange("/api/ride/favorites", HttpMethod.POST, createFavoriteRideRequest, FavoritePathOut.class);
            FavoritePathOut favoritePathResponse = createFavRideResponse.getBody();
            passengerFavoritePaths.add(favoritePathResponse);
        }

    }
    @Test
    @Order(226)
    @DisplayName("Creating favorite ride returns bad request if there are 10 or more rides")
    @Tag("vlada")
    public void createFavoriteRideReturnsBadRequestIfThereTenOrMoreRides(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);

        createFavoriteRides();

        LocationInOut departure = new LocationInOut();
        departure.setLatitude(45);
        departure.setLongitude(45);
        departure.setAddress("Default address");
        LocationInOut destination = new LocationInOut();
        destination.setLatitude(46);
        destination.setLongitude(46);
        destination.setAddress("Default address");


        PathInOut path = new PathInOut();
        path.setDestination(destination);
        path.setDeparture(departure);
        List<PathInOut> paths = List.of(path);

        UserSimplifiedOut passenger = new UserSimplifiedOut();
        passenger.setId(passengerID);
        passenger.setEmail("pera.peric@email.com");

        FavoritePathIn favoritePath = new FavoritePathIn();
        favoritePath.setFavoriteName("Default name");
        favoritePath.setBabyTransport(true);
        favoritePath.setPetTransport(false);
        favoritePath.setVehicleType("STANDARD");
        favoritePath.setLocations(paths);
        favoritePath.setPassengers(List.of(passenger));

        HttpEntity<FavoritePathIn> createFavoriteRideRequest = new HttpEntity<>(favoritePath, headers);

        ResponseEntity<FavoritePathOut> favoriteRideResponse = restTemplate.exchange("/api/ride/favorites", HttpMethod.POST, createFavoriteRideRequest, FavoritePathOut.class);

        assertEquals(HttpStatus.BAD_REQUEST, favoriteRideResponse.getStatusCode());
    }

    @Test
    @Order(227)
    @DisplayName("Create favorite ride returns unauthorized if sender is unauthorized")
    @Tag("vlada")
    public void createFavoriteRideReturnsUnauthorizedIfSenderIsUnauthorized(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<FavoritePathIn> createFavoriteRideRequest = new HttpEntity<>(null, headers);

        ResponseEntity<String> favoriteRideResponse = restTemplate.exchange("/api/ride/favorites", HttpMethod.POST, createFavoriteRideRequest, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, favoriteRideResponse.getStatusCode());
    }

    @Test
    @Order(228)
    @DisplayName("Create favorite ride returns forbidden if sender is not PASSENGER or ADMIN")
    @Tag("vlada")
    public void createFavoriteRideReturnsForbiddenIfSenderIsNotPassengerOrAdmin(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + driver201Token);

        LocationInOut departure = new LocationInOut();
        departure.setLatitude(45);
        departure.setLongitude(45);
        departure.setAddress("Default address");
        LocationInOut destination = new LocationInOut();
        destination.setLatitude(46);
        destination.setLongitude(46);
        destination.setAddress("Default address");


        PathInOut path = new PathInOut();
        path.setDestination(destination);
        path.setDeparture(departure);
        List<PathInOut> paths = List.of(path);

        UserSimplifiedOut passenger = new UserSimplifiedOut();
        passenger.setId(passengerID);
        passenger.setEmail("pera.peric@email.com");

        FavoritePathIn favoritePath = new FavoritePathIn();
        favoritePath.setFavoriteName("Default name");
        favoritePath.setBabyTransport(true);
        favoritePath.setPetTransport(false);
        favoritePath.setVehicleType("STANDARD");
        favoritePath.setLocations(paths);
        favoritePath.setPassengers(List.of(passenger));

        HttpEntity<FavoritePathIn> createFavoriteRideRequest = new HttpEntity<>(favoritePath, headers);

        ResponseEntity<String> cancelRideResponse = restTemplate.exchange("/api/ride/favorites", HttpMethod.POST, createFavoriteRideRequest, String.class);

        assertEquals(HttpStatus.FORBIDDEN, cancelRideResponse.getStatusCode());
    }

    @Test
    @Order(230)
    @DisplayName("Get favorite locations returns list of favorite locations")
    @Tag("vlada")
    public void getFavoriteLocationsReturnsListOfFavoriteLocations(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);

        HttpEntity<FavoritePathIn> createFavoriteRideRequest = new HttpEntity<>(null, headers);

        ResponseEntity<FavoritePathOut[]> favoritePathResponse = restTemplate.exchange("/api/ride/favorites", HttpMethod.GET, createFavoriteRideRequest, FavoritePathOut[].class);
        FavoritePathOut[] favoritePaths = favoritePathResponse.getBody();
        if(favoritePaths == null){
            fail();
        }
        assertEquals(passengerFavoritePaths.size(), favoritePaths.length);
        for(FavoritePathOut favoritePath : favoritePaths){
            assertTrue(passengerFavoritePaths.stream().anyMatch(favoritePathOut -> Objects.equals(favoritePathOut.getId(), favoritePath.getId())));
        }
    }

    @Test
    @Order(300)
    @DisplayName("Shouldnt delete favorite path  when making delete request to endpoint - /api/ride/{id}/favorite")
    public void ShouldDeleteFavorite() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + passengerToken);
        HttpEntity<ReasonIn> deletedRideRequest = new HttpEntity<>(headers);
        Map<String, String> param = new HashMap<String, String>();
        param.put("id","1");
        ResponseEntity<FavoritePathOut> responseEntity = restTemplate.exchange(String.format("/api/ride/favorites/{id}"), HttpMethod.DELETE, deletedRideRequest, FavoritePathOut.class, param);

        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }
}