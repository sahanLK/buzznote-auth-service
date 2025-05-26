package com.buzznote.buzznote.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.buzznote.buzznote.dto.RegisterReq;
import com.buzznote.buzznote.models.User;
import com.buzznote.buzznote.repo.UserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo repo;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(RegisterReq user) {
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        repo.save(newUser);
    }

    public Optional<User> findUserByEmail(String email) {
        return repo.findByEmail(email);
    }
}
