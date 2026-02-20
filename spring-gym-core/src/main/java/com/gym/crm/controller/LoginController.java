package com.gym.crm.controller;

import com.gym.crm.dto.auth.AuthenticationResponseDto;
import com.gym.crm.dto.auth.LoginRequestDto;
import com.gym.crm.dto.auth.UpdatePasswordRequestDto;
import com.gym.crm.exception.AuthenticationFailedException;
import com.gym.crm.security.JwtUtil;
import com.gym.crm.service.LoginAttemptService;
import com.gym.crm.service.TokenBlacklistService;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/auth")
@Tag(name = "Authentication Controller", description = "Endpoints for user login and password changes (Tasks 3 & 4)")
public class LoginController {

    private final TraineeService traineeService;
    private final TrainerService trainerService;

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;

    public LoginController(TraineeService traineeService, TrainerService trainerService,
                           AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
                           JwtUtil jwtUtil, LoginAttemptService loginAttemptService,
                           TokenBlacklistService tokenBlacklistService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.loginAttemptService = loginAttemptService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/login")
    @Operation(summary = "User login (Task 3)", description = "Authenticates a user and returns a JWT Bearer token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, token returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password")
    })
    public ResponseEntity<AuthenticationResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {

        if (loginAttemptService.isBlocked(loginRequest.getUsername())) {
            throw new AuthenticationFailedException("User is blocked for 5 minutes due to too many failed login attempts.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new AuthenticationFailedException("Invalid username or password");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());

        final String token = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthenticationResponseDto(token));
    }

    @PutMapping("/change-password/{username}")
    @Operation(summary = "Change user password (Task 4)", description = "Changes the password for a Trainee or Trainer after validating the old password.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Authentication failed (wrong old password or user not found)")
    })
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "Username of the user") @PathVariable String username,
            @Valid @RequestBody UpdatePasswordRequestDto requestDto) {
        
        boolean traineePassChanged = traineeService.changeTraineePassword(username, requestDto.getOldPassword(), requestDto.getNewPassword());
        
        if (!traineePassChanged) {
            boolean trainerPassChanged = trainerService.changeTrainerPassword(username, requestDto.getOldPassword(), requestDto.getNewPassword());
            
            if (!trainerPassChanged) {
                throw new AuthenticationFailedException("Invalid username or old password");
            }
        }
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user (Task 5)", description = "Blacklists the user's current JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "No token provided or token invalid")
    })
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        final String requestTokenHeader = request.getHeader("Authorization");

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);
            
            tokenBlacklistService.blacklistToken(jwtToken);
            
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.ok().build();
        }
    }
}