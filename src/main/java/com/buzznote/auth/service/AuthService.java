package com.buzznote.auth.service;

import com.buzznote.auth.dto.RegisterRequest;
import com.buzznote.auth.models.User;
import com.buzznote.auth.repo.UserRepo;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private RedisService redisService;

    public static Cookie getCookie(String key, String value, String path) {
        Cookie cookie = new Cookie(key, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath(path);
        cookie.setMaxAge(7 * 24 * 60 * 60);
        return cookie;
    }

    public static Cookie deleteCookie(String key, String value, String path) {
        Cookie cookie = new Cookie(key, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath(path);
        cookie.setMaxAge(0);
        return cookie;
    }

    public void registerUser(RegisterRequest user) {
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(newUser);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public void updateUserPassword(String token, String newPassword) {
        User user = userRepo.findByEmail(redisService.getPasswordResetUserEmail(token)).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }
}
