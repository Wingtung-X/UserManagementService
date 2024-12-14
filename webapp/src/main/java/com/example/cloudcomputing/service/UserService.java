package com.example.cloudcomputing.service;

import com.example.cloudcomputing.exceptoin.UserNotFoundException;
import com.example.cloudcomputing.model.ImageMetadata;
import com.example.cloudcomputing.model.User;
import com.example.cloudcomputing.model.UserToken;
import com.example.cloudcomputing.repository.ImageMetadataRepository;
import com.example.cloudcomputing.repository.UserRepository;
import com.example.cloudcomputing.repository.UserTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.Timer;

import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class UserService {
//    @Autowired
//    UserRepository userRepository;
//    @Autowired
//    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private final Timer findUserByEmailTimer;
    private final Timer saveUserTimer;
    private final Timer updateUserTimer;
    private final UserRepository userRepository;

    private final UserTokenRepository userTokenRepository;
    private final PasswordEncoder passwordEncoder;

//    private final SnsClient snsClient;
//    private final String topicArn;

    @Autowired
    private SnsClient snsClient;

    @Value("${aws.sns.topicArn}")
    private String topicArn;


//    @Autowired
//    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, MeterRegistry meterRegistry, SnsClient snsClient,
//                       @Value("${aws.sns.topicArn}") String topicArn, UserTokenRepository userTokenRepository) {
//
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//        this.userTokenRepository = userTokenRepository;
//
//        this.snsClient = snsClient;
//        this.topicArn = topicArn;
//        // Initialize timers for each database operation
//        this.findUserByEmailTimer = meterRegistry.timer("db.query.find_user_by_email.time");
//        this.saveUserTimer = meterRegistry.timer("db.query.save_user.time");
//        this.updateUserTimer = meterRegistry.timer("db.query.update_user.time");
//    }
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, MeterRegistry meterRegistry, UserTokenRepository userTokenRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userTokenRepository = userTokenRepository;

        // Initialize timers for each database operation
        this.findUserByEmailTimer = meterRegistry.timer("db.query.find_user_by_email.time");
        this.saveUserTimer = meterRegistry.timer("db.query.save_user.time");
        this.updateUserTimer = meterRegistry.timer("db.query.update_user.time");
    }




    public Map<String, String> getUserInfo(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        logger.info("Searching user information for email: {}", email);
//        Optional<User> user = userRepository.findByEmail(email);
        Optional<User> user = findUserByEmailTimer.record(() -> userRepository.findByEmail(email));
        if (user.isPresent()) {
            Map<String, String> map = new HashMap<>();
            User currentUser = user.get();
            map.put("id", currentUser.getId().toString());
            map.put("first_name", currentUser.getFirst_name());
            map.put("last_name", currentUser.getLast_name());
            map.put("email", currentUser.getEmail());
            map.put("account_created", currentUser.getAccount_created().toString());
            map.put("account_updated", currentUser.getAccount_updated().toString());

            logger.info("Get information for user with email: {}", email);

            return map;
        }
        logger.error("User with email {} not found", email);
        throw new UserNotFoundException(email);
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

//    public Map<String, String> createAccount(String email, String password, String first_name, String last_name) {
//
//        logger.info("Creating account for email: {}", email);
//
//        if (email.length() == 0 || password.length() == 0 || first_name.length() == 0 || last_name.length() == 0) {
//
//            logger.error("Invalid account creation request: missing fields");
//
//            throw  new IllegalArgumentException("Please enter email, password, first name and last name");
//        }
////        check valid email address
//        if(!isValidEmail(email)) {
//            logger.error("Invalid email format for email: {}", email);
//            throw new IllegalArgumentException("Invalid email format");
//        }
////        Optional<User> user = userRepository.findByEmail(email);
//        Optional<User> user = findUserByEmailTimer.record(() -> userRepository.findByEmail(email));
//        if (user.isPresent()) {
//            logger.error("Email {} has already been used", email);
//            throw new IllegalArgumentException("Email has been used");
//        }
//        User newUser = new User(email, first_name, last_name, passwordEncoder.encode(password));
////        User curr = userRepository.save(newUser);
//        User curr = saveUserTimer.record(() -> userRepository.save(newUser));
//
////        Optional<User> currentUser = userRepository.findByEmail(email);
//        Map<String, String> map = new HashMap<>();
////        User curr = currentUser.get();
//        map.put("id", curr.getId().toString());
//        map.put("first_name", curr.getFirst_name());
//        map.put("last_name", curr.getLast_name());
//        map.put("email", curr.getEmail());
//        map.put("account_created", curr.getAccount_created().toString());
//        map.put("account_updated", curr.getAccount_updated().toString());
//
//        logger.info("Successfully created account for email: {}", email);
//
//        return map;
//    }


    public Map<String, String> createAccount(String email, String password, String first_name, String last_name) {
        logger.info("Creating account for email: {}", email);

        // 验证输入
        if (email.isEmpty() || password.isEmpty() || first_name.isEmpty() || last_name.isEmpty()) {
            logger.error("Invalid account creation request: missing fields");
            throw new IllegalArgumentException("Please enter email, password, first name, and last name.");
        }

        if (!isValidEmail(email)) {
            logger.error("Invalid email format for email: {}", email);
            throw new IllegalArgumentException("Invalid email format.");
        }

        // 检查用户是否已存在
        Optional<User> user = findUserByEmailTimer.record(() -> userRepository.findByEmail(email));
        if (user.isPresent()) {
            logger.error("Email {} has already been used", email);
            throw new IllegalArgumentException("Email has already been used.");
        }

        // 创建新用户
        User newUser = new User(email, first_name, last_name, passwordEncoder.encode(password));
        User savedUser = saveUserTimer.record(() -> userRepository.save(newUser));

        // generate Token
        String token = UUID.randomUUID().toString();
        UserToken userToken = new UserToken();
        userToken.setUserId(savedUser.getId());
        userToken.setToken(token);
        userToken.setExpiresAt(LocalDateTime.now().plusMinutes(2)); // Token valid for 2 min
        userToken.setIsVerified(false);
        // save token
        userTokenRepository.save(userToken);

        try {
            Map<String, String> snsPayload = new HashMap<>();
            snsPayload.put("email", email);
            snsPayload.put("verificationToken", token);
            snsPayload.put("userId", savedUser.getId().toString());

            String message = new ObjectMapper().writeValueAsString(snsPayload);

            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(message)
                    .build();
            snsClient.publish(publishRequest);

            logger.info("Verification email sent to {} with token {}", email, token);
        } catch (Exception e) {
            logger.error("Failed to send verification email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Verification email sending failed. Please try again later.");
        }

        // 返回用户信息
        Map<String, String> response = new HashMap<>();
        response.put("id", savedUser.getId().toString());
        response.put("first_name", savedUser.getFirst_name());
        response.put("last_name", savedUser.getLast_name());
        response.put("email", savedUser.getEmail());
        response.put("account_created", savedUser.getAccount_created().toString());
        response.put("account_updated", savedUser.getAccount_updated().toString());

        return response;
    }


    public void updateUserInfo(User user){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        logger.info("Attempting to update information for user with email: {}", email);

        Optional<User> mapUser = userRepository.findByEmail(email);

        if (mapUser.isEmpty()) {
            logger.error("User with email {} not found", email);
            throw new UserNotFoundException(email);
        }

        User currentUser = mapUser.get();

        if (!currentUser.getEmail().equals(user.getEmail())) {
            logger.error("User with email {} is not valid", email);
            throw new UserNotFoundException(email);
        }

        if (!user.getFirst_name().equals(currentUser.getFirst_name())) {
            currentUser.setFirst_name(user.getFirst_name());
            currentUser.setAccount_updated(LocalDateTime.now());
            System.out.println("first name updated");
            logger.info("Updated first name for user with email: {}", email);
        }

        if (!user.getLast_name().equals(currentUser.getLast_name())) {
            currentUser.setLast_name(user.getLast_name());
            currentUser.setAccount_updated(LocalDateTime.now());
            System.out.println("last name updated");
            logger.info("Updated last name for user with email: {}", email);
        }

        if (!BCrypt.checkpw(user.getPassword(), currentUser.getPassword())) {
            currentUser.setPassword(passwordEncoder.encode(user.getPassword()));
            currentUser.setAccount_updated(LocalDateTime.now());
            System.out.println("password updated");
            logger.info("Updated password for user with email: {}", email);
        }

//        userRepository.save(currentUser);
        saveUserTimer.record(() -> userRepository.save(currentUser));
        logger.info("Successfully updated information for user with email: {}", email);
    }

    public boolean verifiedLink(String userid){
        Optional<UserToken> user = userTokenRepository.findByUserId(userid);
        return user.get().getIsVerified();
    }

//    public void updateFirstName(String firstName) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String email = authentication.getName();
//        Optional<User> user = userRepository.findByEmail(email);
//        if (user.isEmpty()) {
//            throw new UserNotFoundException(email);
//        }
//        User currentUser = user.get();
//        currentUser.setFirstName(firstName);
//        currentUser.setAccount_updated(LocalDateTime.now());
//        userRepository.save(currentUser);
//    }
//
//    public void updateLastName(String lastName) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String email = authentication.getName();
//        Optional<User> user = userRepository.findByEmail(email);
//        if (user.isEmpty()) {
//            throw new UserNotFoundException(email);
//        }
//        User currentUser = user.get();
//        currentUser.setLastName(lastName);
//        currentUser.setAccount_updated(LocalDateTime.now());
//        userRepository.save(currentUser);
//    }
//
//    public void updatePassword(String password) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String email = authentication.getName();
//        Optional<User> user = userRepository.findByEmail(email);
//        if (user.isEmpty()) {
//            throw new UserNotFoundException(email);
//        }
//        User currentUser = user.get();
//        currentUser.setPassword(passwordEncoder.encode(password));
//        currentUser.setAccount_updated(LocalDateTime.now());
//        userRepository.save(currentUser);
//    }



}
