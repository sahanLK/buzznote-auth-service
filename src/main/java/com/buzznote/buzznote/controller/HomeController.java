package com.buzznote.buzznote.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping("")
    public String home(HttpServletRequest req) {
        return "Welcome to Buzznote" + req.getSession().getId();
    }

    @GetMapping("/csrf-token")
    public CsrfToken getCsrfToken(HttpServletRequest request) {
        return  (CsrfToken) request.getAttribute("_csrf");
    }
}
