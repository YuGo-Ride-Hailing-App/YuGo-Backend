package org.yugo.backend.YuGo.controllers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.yugo.backend.YuGo.dto.Login;
import org.yugo.backend.YuGo.dto.RideDetailedOut;
import org.yugo.backend.YuGo.dto.TokenStateOut;

import static org.junit.jupiter.api.Assertions.assertEquals;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        locations = "classpath:application-test.properties")
@ActiveProfiles("test")
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

    @Test
    @DisplayName("Should find active ride for driver when making GET request to endpoint - /api/ride/driver/{id}/active")
    public void ShouldFindActiveRideForDriver() {
        ResponseEntity<RideDetailedOut> responseEntity = restTemplate.getForEntity("/api/ride/driver/6/active", RideDetailedOut.class);
        RideDetailedOut activeRide = responseEntity.getBody();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(1, activeRide.getId());
    }

    @Test
    @DisplayName("Shouldn't find active ride for driver when making GET request to endpoint - /api/ride/driver/{id}/active")
    public void ShouldNotFindActiveRideForDriver() {
        ResponseEntity<RideDetailedOut> responseEntity = restTemplate.getForEntity("/api/ride/driver/7/active", RideDetailedOut.class);

        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }
}