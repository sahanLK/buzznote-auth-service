package com.buzznote.auth.service;

import java.util.Optional;

import jakarta.servlet.http.Cookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.buzznote.auth.dto.RegisterRequest;
import com.buzznote.auth.models.User;
import com.buzznote.auth.repo.UserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo repo;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(RegisterRequest user) {
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        repo.save(newUser);
    }

    public static Cookie getCookie(String key, String value, String path) {
        Cookie cookie = new Cookie(key, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath(path);
        cookie.setMaxAge(7 * 24 * 60 * 60);
        return cookie;
    }

    public Optional<User> findUserByEmail(String email) {
        return repo.findByEmail(email);
    }
}
