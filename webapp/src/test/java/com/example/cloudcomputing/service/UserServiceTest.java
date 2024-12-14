//package com.example.cloudcomputing.service;
//////
//////import com.example.cloudcomputing.model.User;
//////import com.example.cloudcomputing.repository.UserRepository;
//////import org.junit.jupiter.api.BeforeEach;
//////import org.junit.jupiter.api.Test;
//////import org.mockito.ArgumentMatchers;
//////import org.mockito.InjectMocks;
//////import org.mockito.Mock;
//////import org.mockito.MockitoAnnotations;
//////import org.springframework.security.crypto.password.PasswordEncoder;
//////import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
//////
//////import java.util.Map;
//////import java.util.Optional;
//////import java.util.UUID;
//////
////////import static org.hamcrest.Matchers.any;
//////import static org.junit.jupiter.api.Assertions.assertEquals;
//////import static org.junit.jupiter.api.Assertions.assertThrows;
//////import static org.mockito.ArgumentMatchers.anyString;
//////import static org.mockito.ArgumentMatchers.any;
//////import static org.mockito.Mockito.*;
//////
//////import io.micrometer.core.instrument.MeterRegistry;
//////import io.micrometer.core.instrument.Timer;
//////
//////public class UserServiceTest {
//////
//////    @Mock
//////    private UserRepository userRepository;
//////
//////    @Mock
//////    private PasswordEncoder passwordEncoder;
//////
//////    @Mock
//////    private MeterRegistry meterRegistry;
//////
//////    @Mock
//////    private Timer timer;
//////
//////    @InjectMocks
//////    private UserService userService;
//////
//////    @BeforeEach
//////    void setUp() {
//////        MockitoAnnotations.openMocks(this);
//////
//////        // 使用 SimpleMeterRegistry 替代 mock 的 MeterRegistry
//////        MeterRegistry meterRegistry = new SimpleMeterRegistry();
//////
//////        userService = new UserService(userRepository, passwordEncoder, meterRegistry);
//////    }
//////
//////
//////    @Test
//////    void testInvalidEmailFormat() {
//////        String invalidEmail = "11111";
//////        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
//////            userService.createAccount(invalidEmail, "password", "first", "last");
//////        });
//////        assertEquals("Invalid email format", exception.getMessage());
//////    }
//////
//////    @Test
//////    void testEmailAlreadyUsed() {
//////        String email = "test@example.com";
//////
//////        // email already exists
//////        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));
//////
//////        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
//////            userService.createAccount(email, "password", "John", "Doe");
//////        });
//////
//////        assertEquals("Email has been used", exception.getMessage());
//////    }
//////
//////    @Test
//////    void testEmptyFields() {
//////        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
//////            userService.createAccount("", "password", "John", "Doe");
//////        });
//////
//////        assertEquals("Please enter email, password, first name and last name", exception.getMessage());
//////    }
//////
//////    @Test
//////    void testSuccessfulAccountCreation() {
//////        String email = "newuser@example.com";
//////        String password = "password";
//////        String encodedPassword = "encodedPassword";
//////
//////        // 1. email not exits
//////        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
//////
//////        // 2. encrypt password
//////        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
//////
//////        // 3. save user
//////        User savedUser = new User(email, "John", "Doe", encodedPassword);
//////        when(userRepository.save(ArgumentMatchers.any(User.class))).thenReturn(savedUser);
//////
//////        // 4. validate account created successfully
//////        Map<String, String> userInfo = userService.createAccount(email, password, "John", "Doe");
//////
//////        assertEquals(email, userInfo.get("email"));
//////        assertEquals("John", userInfo.get("first_name"));
//////        assertEquals("Doe", userInfo.get("last_name"));
//////    }
//////
//////}
////
//////--------------------new
//
//import com.example.cloudcomputing.model.User;
//import com.example.cloudcomputing.model.UserToken;
//import com.example.cloudcomputing.repository.UserRepository;
//import com.example.cloudcomputing.repository.UserTokenRepository;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import software.amazon.awssdk.services.sns.SnsClient;
//import software.amazon.awssdk.services.sns.model.PublishRequest;
//
//import java.util.Map;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//public class UserServiceTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private UserTokenRepository userTokenRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private SnsClient snsClient; // Mock SnsClient
//
//
//    @InjectMocks
//    private UserService userService;
//
//    private String topicArn;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        // 使用 SimpleMeterRegistry 替代 MeterRegistry
//        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
//
//        String topicArn = "arn:aws:sns:us-east-1:123456789012:MyTopic";
//
//        userService = new UserService(
//                userRepository,
//                passwordEncoder,
//                meterRegistry, // 使用非空的 MeterRegistry
//
//                userTokenRepository
//        );
//    }
//
//
//    @Test
//    void testInvalidEmailFormat() {
//        String invalidEmail = "11111";
//        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
//            userService.createAccount(invalidEmail, "password", "first", "last");
//        });
//        assertEquals("Invalid email format", exception.getMessage());
//    }
//
//    @Test
//    void testEmailAlreadyUsed() {
//        String email = "test@example.com";
//
//        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));
//
//        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
//            userService.createAccount(email, "password", "John", "Doe");
//        });
//
//        assertEquals("Email has been used", exception.getMessage());
//    }
//
//    @Test
//    void testEmptyFields() {
//        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
//            userService.createAccount("", "password", "John", "Doe");
//        });
//
//        assertEquals("Please enter email, password, first name and last name", exception.getMessage());
//    }
//
//    @Test
//    void testSuccessfulAccountCreation() throws Exception {
//        String email = "newuser@example.com";
//        String password = "password";
//        String encodedPassword = "encodedPassword";
//        UUID userId = UUID.randomUUID();
//        String token = UUID.randomUUID().toString();
//
//        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
//        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
//
//        User savedUser = new User(email, "John", "Doe", encodedPassword);
//        savedUser.setId(userId.toString());
//        when(userRepository.save(any(User.class))).thenReturn(savedUser);
//
//        doNothing().when(userTokenRepository).save(any());
//
//        // Mock snsClient 的 publish 方法
//        when(snsClient.publish(any(PublishRequest.class))).thenReturn(null);
//
//        // 调用 UserService
//        Map<String, String> userInfo = userService.createAccount(email, password, "John", "Doe");
//
//        // 验证返回值
//        assertEquals(email, userInfo.get("email"));
//        assertEquals("John", userInfo.get("first_name"));
//        assertEquals("Doe", userInfo.get("last_name"));
//
//        // 验证 SNS 调用
//        ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
//        verify(snsClient, times(1)).publish(captor.capture());
//        PublishRequest capturedRequest = captor.getValue();
//
//        // 验证 PublishRequest 内容
//        assertEquals(topicArn, capturedRequest.topicArn());
//        String message = capturedRequest.message();
//        Map<String, String> payload = new ObjectMapper().readValue(message, Map.class);
//        assertEquals(email, payload.get("email"));
//        assertEquals(userId.toString(), payload.get("userId"));
//        assertNotNull(payload.get("verificationToken"));
//    }
//
//}
//


package com.example.cloudcomputing.service;

import com.example.cloudcomputing.model.User;
import com.example.cloudcomputing.repository.UserRepository;
import com.example.cloudcomputing.repository.UserTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

//import static org.hamcrest.Matchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserTokenRepository userTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Timer timer;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 使用 SimpleMeterRegistry 替代 mock 的 MeterRegistry
        MeterRegistry meterRegistry = new SimpleMeterRegistry();

        userService = new UserService(userRepository, passwordEncoder, meterRegistry, userTokenRepository);
    }


    @Test
    void testInvalidEmailFormat() {
        String invalidEmail = "11111";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createAccount(invalidEmail, "password", "first", "last");
        });
        assertEquals("Invalid email format.", exception.getMessage());
    }

    @Test
    void testEmailAlreadyUsed() {
        String email = "test@example.com";

        // email already exists
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createAccount(email, "password", "John", "Doe");
        });

        assertEquals("Email has already been used.", exception.getMessage());
    }

    @Test
    void testEmptyFields() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createAccount("", "password", "John", "Doe");
        });

        assertEquals("Please enter email, password, first name, and last name.", exception.getMessage());
    }

//    @Test
//    void testSuccessfulAccountCreation() {
//        String email = "newuser@example.com";
//        String password = "password";
//        String encodedPassword = "encodedPassword";
//
//        // 1. email not exits
//        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
//
//        // 2. encrypt password
//        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
//
//        // 3. save user
//        User savedUser = new User(email, "John", "Doe", encodedPassword);
//        when(userRepository.save(ArgumentMatchers.any(User.class))).thenReturn(savedUser);
//
//        // 4. validate account created successfully
//        Map<String, String> userInfo = userService.createAccount(email, password, "John", "Doe");
//
//        assertEquals(email, userInfo.get("email"));
//        assertEquals("John", userInfo.get("first_name"));
//        assertEquals("Doe", userInfo.get("last_name"));
//    }

}
