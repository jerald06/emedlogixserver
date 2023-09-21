package com.emedlogix.controller;

import com.emedlogix.entity.JwtRequest;
import com.emedlogix.entity.UserVO;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;

@RestController
@CrossOrigin
public interface JwtAuthenticationController {
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<?> saveUser(@RequestBody UserVO user) throws Exception;

    @RequestMapping(value = "/forgot-password", method = RequestMethod.POST)
    public String forgotPassword(@RequestParam("email") String email, Model model);

    @GetMapping("/get-otp")
    public String getOtp(@RequestParam("email") String email);

    @RequestMapping(value = "/verify-otp", method = RequestMethod.POST)
    public String verifyOTP(@RequestParam("email") String email, @RequestParam("otp") String enteredOTP, Model model);

    @Transactional
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam("email") String email, @RequestParam("password") String newPassword);
}