package com.example.cloudcomputing.controller;

import com.example.cloudcomputing.service.UserService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RestController
public class HealthCheckController {

    private final DataSource dataSource;
    private final Counter healthCheckCounter;
    private final Timer healthCheckTimer;

    public static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public HealthCheckController(DataSource dataSource, MeterRegistry meterRegistry) {
        this.dataSource = dataSource;
        logger.info("data sorce {}", dataSource.toString());
        this.healthCheckCounter = meterRegistry.counter("health_check_get_api_count");
        this.healthCheckTimer = meterRegistry.timer("health_check_get_api_time");
    }

    @GetMapping("/healthz")
    public ResponseEntity<Void> healthCheck(
            @RequestBody(required = false) String body,
            @RequestParam(required = false) String param
    ) {

        long startTime = System.currentTimeMillis();
        healthCheckCounter.increment(); // increase the counter

        try {
            if (body != null || param != null) {
                logger.error("Req body or param contains element.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("Cache-Control", "no-cache, no-store, must-revalidate;")
                        .header("Pragma", "no-cache")
                        .header("X-Content-Type-Options", "nosniff")
                        .build();
            }

            try (Connection connection = dataSource.getConnection()) {
                if (!connection.isClosed()) {
                    return ResponseEntity.ok()
                            .header("Cache-Control", "no-cache, no-store, must-revalidate;")
                            .header("Pragma", "no-cache")
                            .header("X-Content-Type-Options", "nosniff")
                            .build();
                }
            } catch (SQLException e) {
                logger.error("DB not connected.");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .header("Cache-Control", "no-cache, no-store, must-revalidate;")
                        .header("Pragma", "no-cache")
                        .header("X-Content-Type-Options", "nosniff")
                        .build();
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .header("Cache-Control", "no-cache, no-store, must-revalidate;")
                    .header("Pragma", "no-cache")
                    .header("X-Content-Type-Options", "nosniff")
                    .build();
        } finally {
            healthCheckTimer.record((System.currentTimeMillis() - startTime), TimeUnit.MILLISECONDS);
        }
    }

    @RequestMapping(value = "/healthz", method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.HEAD})
    public ResponseEntity<Void> httpMethodCheck(){
        logger.error("Invalid method");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .header("Cache-Control","no-cache, no-store, must-revalidate;")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff")
                .build();
    }



    @GetMapping("/cicd")
    public ResponseEntity<Void> cicdTestCheck(
            @RequestBody(required = false) String body,
            @RequestParam(required = false) String param
    ) {

        long startTime = System.currentTimeMillis();
        healthCheckCounter.increment(); // increase the counter

        try {
            if (body != null || param != null) {
                logger.error("Req body or param contains element.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("Cache-Control", "no-cache, no-store, must-revalidate;")
                        .header("Pragma", "no-cache")
                        .header("X-Content-Type-Options", "nosniff")
                        .build();
            }

            try (Connection connection = dataSource.getConnection()) {
                if (!connection.isClosed()) {
                    return ResponseEntity.ok()
                            .header("Cache-Control", "no-cache, no-store, must-revalidate;")
                            .header("Pragma", "no-cache")
                            .header("X-Content-Type-Options", "nosniff")
                            .build();
                }
            } catch (SQLException e) {
                logger.error("DB not connected.");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .header("Cache-Control", "no-cache, no-store, must-revalidate;")
                        .header("Pragma", "no-cache")
                        .header("X-Content-Type-Options", "nosniff")
                        .build();
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .header("Cache-Control", "no-cache, no-store, must-revalidate;")
                    .header("Pragma", "no-cache")
                    .header("X-Content-Type-Options", "nosniff")
                    .build();
        } finally {
            healthCheckTimer.record((System.currentTimeMillis() - startTime), TimeUnit.MILLISECONDS);
        }
    }
}

