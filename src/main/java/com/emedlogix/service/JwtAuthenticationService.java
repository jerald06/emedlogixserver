package com.emedlogix.service;

import com.emedlogix.controller.JwtAuthenticationController;
import com.emedlogix.entity.JwtRequest;
import com.emedlogix.entity.JwtResponse;
import com.emedlogix.entity.User;
import com.emedlogix.entity.UserVO;
import com.emedlogix.repository.UserRepository;
import com.emedlogix.security.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Random;

@Service
public class JwtAuthenticationService implements JwtAuthenticationController {

    private static final int OTP_LENGTH = 6;

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Override
    public ResponseEntity<?> createAuthenticationToken(JwtRequest authenticationRequest) throws Exception {
        authenticate(authenticationRequest.getEmail(), authenticationRequest.getPassword());

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getEmail());

        final String token = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(token));
    }

    @Override
    public ResponseEntity<?> saveUser(UserVO user) throws Exception {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        if (!user.getPassword().equals(user.getConfirm_password())) {
            return ResponseEntity.badRequest().body("Passwords do not match");
        }
        if (!user.getEmail().matches("^[a-zA-Z0-9\\.]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$")) {
            return ResponseEntity.badRequest().body("Invalid email format or characters");
        }
        if (!user.getPassword().matches("^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")) {
            return ResponseEntity.badRequest().body("Invalid password format");
        }

        userDetailsService.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @Override
    public String forgotPassword(String email, Model model) {
        User user = userRepository.findByEmail(email);

        if (user != null) {
            // Generate OTP and store in the database for the user
            String otp = generateRandomOTP();
            user.setOtp(otp);
            userRepository.save(user);

            // Send OTP to the user's email (implement this)
            sendOTPByEmail(email, otp);

            model.addAttribute("message", "An OTP has been sent to your email.");
            return "otp-verification";
        } else {
            model.addAttribute("error", "Email not found.");
            return "forgot-password";
        }
    }

    @Override
    public String getOtp(String email) {
        User user = userRepository.findByEmail(email);

        if (user != null) {
            String otp = user.getOtp();
            return otp != null ? otp : "OTP not found";
        } else {
            return "User not found";
        }
    }

    @Override
    public String verifyOTP(String email, String enteredOTP, Model model) {
        User user = userRepository.findByEmail(email);

        if (user != null) {
            String storedOTP = user.getOtp();

            if (storedOTP != null && storedOTP.equals(enteredOTP)) {
                return "otp-verification-success"; // Redirect to a success page
            } else {
                model.addAttribute("error", "Invalid OTP. Please try again.");
                return "otp-verification"; // Show OTP verification form again with an error message
            }
        } else {
            model.addAttribute("error", "Email not found.");
            return "forgot-password"; // Redirect back to the forgot password page with an error message
        }
    }

    // Generate a random OTP
    private String generateRandomOTP() {

        StringBuilder otp = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10)); // Generates a random digit (0-9)
        }

        return otp.toString();
    }

    // Send OTP to the user's email (Implement this)
    private void sendOTPByEmail(String email, String otp) {
        String subject = "Your OTP for Password Reset";
        String message = "Your OTP is: " + otp;

        SimpleMailMessage emailMessage = new SimpleMailMessage();
        emailMessage.setTo(email);
        emailMessage.setSubject(subject);
        emailMessage.setText(message);

        mailSender.send(emailMessage);
    }


    @Override
    public ResponseEntity<String> resetPassword(String email, String newPassword) {
        // Use your userService or userRepository to find the user by email
        User user = userRepository.findByEmail(email);


        if (user != null) {
            // Update the user's password
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String hashedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(hashedPassword);
            userRepository.save(user);
            return ResponseEntity.ok("password-reset-success");
        } else {
            return ResponseEntity.ok("login");
        }
    }

    private void authenticate(String email, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}