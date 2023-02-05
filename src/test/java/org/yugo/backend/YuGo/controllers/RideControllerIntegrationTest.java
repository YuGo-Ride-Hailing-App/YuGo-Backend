package org.yugo.backend.YuGo.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.yugo.backend.YuGo.dto.RideDetailedOut;

import static org.junit.jupiter.api.Assertions.assertEquals;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        locations = "classpath:application-test.properties")
@ActiveProfiles("test")
public class RideControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Should find first active ride for driver when making GET request to endpoint - /api/ride/driver/{id}/active")
    public void ShouldFindActiveRideForDriver() {
        ResponseEntity<RideDetailedOut> responseEntity = restTemplate.exchange("/api/ride/driver/6/active",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        RideDetailedOut activeRide = responseEntity.getBody();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(1, activeRide.getId());
    }
}