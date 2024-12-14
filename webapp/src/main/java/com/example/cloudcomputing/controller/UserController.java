package com.example.cloudcomputing.controller;

import com.example.cloudcomputing.exceptoin.UserNotFoundException;
import com.example.cloudcomputing.model.User;
import com.example.cloudcomputing.model.UserToken;
import com.example.cloudcomputing.repository.UserRepository;
import com.example.cloudcomputing.repository.UserTokenRepository;
import com.example.cloudcomputing.service.UserService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.example.cloudcomputing.controller.HealthCheckController.logger;


@RestController
@RequestMapping
public class UserController {

    @Autowired
    private UserService userService;

    private final Counter getUserInfoCounter;
    private final Counter createUserCounter;
    private final Counter updateUserInfoCounter;
    private final Timer getUserInfoTimer;
    private final Timer createUserTimer;
    private final Timer updateUserInfoTimer;

    @Autowired
    private UserTokenRepository userTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public UserController(MeterRegistry meterRegistry) {
        // Initialize counters
        this.getUserInfoCounter = meterRegistry.counter("api.v1.user.self.get.count");
        this.createUserCounter = meterRegistry.counter("api.v1.user.post.count");
        this.updateUserInfoCounter = meterRegistry.counter("api.v1.user.self.put.count");

        // Initialize timers
        this.getUserInfoTimer = meterRegistry.timer("api.v1.user.self.get.time");
        this.createUserTimer = meterRegistry.timer("api.v1.user.post.time");
        this.updateUserInfoTimer = meterRegistry.timer("api.v1.user.self.put.time");
    }

    @GetMapping("/v1/user/self")
    public ResponseEntity<?> getUserInfo() {
        getUserInfoCounter.increment(); // Count the number of times this API is called
        long startTime = System.currentTimeMillis();
        try {
            Map<String, String> userInfo = userService.getUserInfo();

            // 获取用户ID（假设UserService返回的Map包含userId字段）
            String userId = userInfo.get("id");

            // 检查用户的token是否已验证
            boolean isVerified = userService.verifiedLink(userId);
            if (!isVerified) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.status(HttpStatus.OK).body(userInfo);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } finally {
            getUserInfoTimer.record(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS); // Record time taken
        }
    }

    @PostMapping("v1/user")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        createUserCounter.increment(); // Count the number of times this API is called
        long startTime = System.currentTimeMillis();

        try {
            Map<String, String> userInfo = userService.createAccount(user.getEmail(), user.getPassword(), user.getFirst_name(), user.getLast_name());
            return ResponseEntity.status(HttpStatus.CREATED).body(userInfo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } finally {
            createUserTimer.record(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS); // Record time taken
        }
    }

    @PutMapping("v1/user/self")
    public ResponseEntity updateUserInfo(@RequestBody User user) {
        updateUserInfoCounter.increment(); // Count the number of times this API is called
        long startTime = System.currentTimeMillis();

        if (user == null || user.getEmail() == null || user.getAccount_created() != null
                || user.getAccount_updated() != null || user.getFirst_name() == null
                || user.getLast_name() == null || user.getPassword() == null
                || user.getPassword().length() == 0 || user.getLast_name().length() == 0
                || user.getFirst_name().length() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
//        System.out.println("here! user id: " + userRepository.findByEmail(user.getEmail()).get().getId());

        boolean isVerified = userService.verifiedLink(userRepository.findByEmail(user.getEmail()).get().getId());
        if (!isVerified) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            userService.updateUserInfo(user);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } finally {
            updateUserInfoTimer.record(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS); // Record time taken
        }
    }

    @RequestMapping(value = "/v1/user/self", method = {RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.HEAD})
    public ResponseEntity<Void> httpMethodCheck() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

//    verification_link = f"https://dev.yingtongcsye6225.me/v1/user/verify?token={token}"

    @GetMapping("/v1/user/verify")
    public ResponseEntity<String> verifyUser(@RequestParam String token) {
        logger.info("Received verification request for token: {}", token);

        // find token
        Optional<UserToken> userTokenOptional = userTokenRepository.findByToken(token);

        if (userTokenOptional.isEmpty()) {
            logger.error("Invalid or expired token: {}", token);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token.");
        }

        UserToken userToken = userTokenOptional.get();

        // check expire
        if (userToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            logger.error("Token expired for userId: {}", userToken.getUserId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token has expired.");
        }

        // verified = true
        userToken.setIsVerified(true);
        userTokenRepository.save(userToken);

        logger.info("User with userId: {} has been successfully verified.", userToken.getUserId());
        return ResponseEntity.ok("User has been successfully verified.");
    }

}

