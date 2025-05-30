package com.buzznote.auth.security;

import com.buzznote.auth.controller.AuthController;
import com.buzznote.auth.models.User;
import com.buzznote.auth.repo.UserRepo;
import com.buzznote.auth.service.AuthService;
import com.buzznote.auth.service.JwtService;
import com.buzznote.auth.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private RedisService redisService;

    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = new User();
        user.setEmail(email);

        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = jwtService.createRefreshToken(user);

        redisService.setAccessToken(user.getEmail(), accessToken);
        redisService.setRefreshToken(user.getEmail(), refreshToken);

        response.addCookie(AuthService.getCookie("refreshToken", refreshToken, "/api/auth/refresh-token"));
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"token\": \"" + accessToken + "\"}");
    }
}