package ru.castroy10.springjwt.controller;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.castroy10.springjwt.model.dto.LoginRequestDto;
import ru.castroy10.springjwt.model.dto.ResponseDto;
import ru.castroy10.springjwt.service.SecurityService;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final SecurityService securityService;

    /**
     * Handles requests to the root URL ("/").
     *
     * @return the name of the index view template
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Authenticates a user and returns access and refresh tokens.
     *
     * @param loginRequest the login request body containing credentials
     * @return a ResponseEntity containing the operation result and tokens
     */
    @PostMapping("/api/v1/login")
    @ResponseBody
    public ResponseEntity<@NonNull ResponseDto> login(@RequestBody final LoginRequestDto loginRequest) {
        return ResponseEntity.ok(
                new ResponseDto("Login successful", securityService.login(loginRequest)));
    }

    /**
     * Refreshes the access token using a valid refresh token.
     *
     * @param authHeader the Authorization header containing the Bearer refresh token
     * @return a ResponseEntity containing the operation result and new tokens
     */
    @PostMapping("/api/v1/refresh")
    @ResponseBody
    public ResponseEntity<@NonNull ResponseDto> refresh(@RequestHeader("Authorization") final String authHeader) {
        return ResponseEntity.ok(
                new ResponseDto("Tokens updated successful", securityService.refresh(authHeader)));
    }

}
