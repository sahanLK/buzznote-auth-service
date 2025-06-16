package com.buzznote.auth.controller;

import com.buzznote.auth.dto.*;
import com.buzznote.auth.models.User;
import com.buzznote.auth.service.AuthService;
import com.buzznote.auth.service.JwtService;
import com.buzznote.auth.service.RabbitEmailService;
import com.buzznote.auth.service.RedisService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RabbitEmailService rabbitEmailService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest user, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
            if (!authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials");
            }

            User fetchedUser = authService.findUserByEmail(user.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            String accessToken = jwtService.createAccessToken(fetchedUser);
            String refreshToken = jwtService.createRefreshToken(fetchedUser);
            response.addCookie(AuthService.getCookie("refreshToken", refreshToken, "/api/auth/refresh-token"));
            response.addCookie(AuthService.getCookie("accessToken", accessToken, "/"));

            redisService.setAccessToken(fetchedUser.getEmail(), accessToken);
            redisService.setRefreshToken(fetchedUser.getEmail(), refreshToken);
            logger.info("Login success: {}", fetchedUser.getEmail());
            return ResponseEntity.ok(new LoginResponse(accessToken));

        } catch (AuthenticationException e) {
            logger.info("Login failed: {} -> {}", user.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest user) {
        try {
            authService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }
    }

    @GetMapping("/user-details")
    public ResponseEntity<?> userDetails(Principal user) {
        return ResponseEntity.ok().body(user);
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@CookieValue String refreshToken, HttpServletResponse response) {
        User user = authService.findUserByEmail(jwtService.extractUsername(refreshToken)).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (jwtService.validateRefreshToken(refreshToken)) {
            if (redisService.isValidRefreshToken(user.getEmail(), refreshToken)) {
                String newRefreshToken = jwtService.createRefreshToken(user);
                String newAccessToken = jwtService.createAccessToken(user);
                response.addCookie(AuthService.getCookie("accessToken", newAccessToken, "/"));
                response.addCookie(AuthService.getCookie("refreshToken", newRefreshToken, "/api/auth/refresh-token"));

                redisService.setAccessToken(user.getEmail(), newAccessToken);
                redisService.setRefreshToken(user.getEmail(), newRefreshToken);
                logger.info("Token refresh successful: {}", user.getEmail());
                return ResponseEntity.ok().body(new LoginResponse(newAccessToken));
            }
        }
        logger.info("Invalid refresh token from: {}", user.getEmail());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please sign in again");
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(auth.getPrincipal());
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        redisService.removeUser(userDetails.getUsername());

        response.addCookie(AuthService.deleteCookie("refreshToken", null, "/api/auth/refresh-token"));
        response.addCookie(AuthService.deleteCookie("accessToken", null, "/"));
        return ResponseEntity.status(200).body("Logged out");
    }

    @PostMapping("/reset-password/init")
    public ResponseEntity<?> resetPasswordInit(@RequestBody PasswordResetInitRequest body) {
        EmailValidator validator = EmailValidator.getInstance();
        if (validator.isValid(body.getEmail())) {
            Optional<User> user = authService.findUserByEmail(body.getEmail());
            if (user.isPresent()) {
                rabbitEmailService.sendPasswordResetEmail(body.getEmail());
                return ResponseEntity.ok().body("Email sent. please check your inbox.");
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please enter a valid email");
    }

    @PostMapping("/reset-password/validate")
    public ResponseEntity<?> validatePasswordResetToken(@RequestBody PasswordResetValidateRequest body) {
        if (redisService.validatePasswordResetToken(body.getToken())) {
            return ResponseEntity.ok().body(new PasswordResetRequestResponse(body.getToken(), true));
        }
        return ResponseEntity.ok().body(new PasswordResetRequestResponse(body.getToken(), false));
    }

    @PostMapping("/reset-password/complete")
    public ResponseEntity<?> resetPasswordComplete(@RequestBody PasswordResetCompleteRequest body) {
        if (redisService.validatePasswordResetToken(body.getToken())) {
            authService.updateUserPassword(body.getToken(), body.getNewPassword());
        }
        return ResponseEntity.ok().body("Password changed successfully.");
    }
}
