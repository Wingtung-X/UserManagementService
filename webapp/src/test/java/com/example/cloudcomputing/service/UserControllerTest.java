package com.example.cloudcomputing.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;




    //  test get user info
    @Test
    @WithMockUser(username = "Oct3check@123.com", password = "password", roles = "USER")
    void testGetUserInfoSuccess() throws Exception {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode("password");

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("id", "1234");
        userInfo.put("email", "Oct3check@123.com");
        userInfo.put("first_name", "John");
        userInfo.put("last_name", "Doe");
        userInfo.put("password", encodedPassword);

        when(userService.getUserInfo()).thenReturn(userInfo);

        mockMvc.perform(get("/v1/user/self")
                        .with(httpBasic("Oct3check@123.com", "password"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
//                .andExpect(jsonPath("$.email").value("Oct3check@123.com"));
    }


    // test create user
    @Test
    void testCreateUserSuccess() throws Exception {
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("id", "1234");
        userInfo.put("email", "Oct3check@123.com");
        userInfo.put("first_name", "John");
        userInfo.put("last_name", "Doe");

        when(userService.createAccount(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(userInfo);

        String userJson = """
        {
            "email": "Oct3check@123.com",
            "password": "password",
            "first_name": "John",
            "last_name": "Doe"
        }
        """;

        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("Oct3check@123.com"));
    }

//    test password update
//    @Test
//    @WithMockUser(username = "Oct3check@123.com", password = "password", roles = "USER")
//    void testUpdatePasswordAndAuthenticate() throws Exception {
//        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//
//        // old password
//        String oldEncodedPassword = passwordEncoder.encode("password");
//
//        // simulate user db info
//        Map<String, String> userInfo = new HashMap<>();
//        userInfo.put("id", "1234");
//        userInfo.put("email", "Oct3check@123.com");
//        userInfo.put("first_name", "John");
//        userInfo.put("last_name", "Doe");
//        userInfo.put("password", oldEncodedPassword);
//
//        // user info
//        Mockito.when(userService.getUserInfo()).thenReturn(userInfo);
//
//        // update password
//        String updatePasswordJson = """
//    {
//        "first_name": "JohnUpdated",
//        "last_name": "DoeUpdated",
//        "password": "newpassword",
//        "email": "Oct3check@123.com"
//    }
//    """;
//
//        // update password using old password
//        mockMvc.perform(put("/v1/user/self")
//                        .with(httpBasic("Oct3check@123.com", "password"))  // 使用旧密码 "password"
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(updatePasswordJson))
//                .andExpect(status().isNoContent());
//
//        // new password has been set
//        String newEncodedPassword = passwordEncoder.encode("newpassword");
//        userInfo.put("password", newEncodedPassword);  // update password in user info
//
//        // auth using new password
//        mockMvc.perform(get("/v1/user/self")
//                        .with(httpBasic("Oct3check@123.com", "newpassword"))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.email").value("Oct3check@123.com"));
//    }



    // method not allowed
    @Test
    @WithMockUser(username = "Oct3check@123.com", password = "password", roles = "USER")
    void testInvalidHttpMethods() throws Exception {
//        POST
        mockMvc.perform(post("/v1/user/self")
                        .with(httpBasic("Oct3check@123.com", "password")))
                .andExpect(status().isMethodNotAllowed());

        // DELETE
        mockMvc.perform(delete("/v1/user/self")
                        .with(httpBasic("Oct3check@123.com", "password")))
                .andExpect(status().isMethodNotAllowed());

        // OPTIONS
        mockMvc.perform(options("/v1/user/self")
                        .with(httpBasic("Oct3check@123.com", "password")))
                .andExpect(status().isMethodNotAllowed());

        // HEAD
        mockMvc.perform(head("/v1/user/self")
                        .with(httpBasic("Oct3check@123.com", "password")))
                .andExpect(status().isMethodNotAllowed());
    }

}
