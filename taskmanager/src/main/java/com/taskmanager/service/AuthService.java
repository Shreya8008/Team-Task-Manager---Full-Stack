package com.taskmanager.service;

import com.taskmanager.dto.AuthDto;
import com.taskmanager.entity.User;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.JwtUtils;
import com.taskmanager.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Transactional
    public AuthDto.AuthResponse signup(AuthDto.SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        // First user becomes ADMIN
        long userCount = userRepository.count();
        User.Role role = userCount == 0 ? User.Role.ADMIN : User.Role.MEMBER;

        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .globalRole(role)
            .build();

        userRepository.save(user);

        String token = jwtUtils.generateTokenFromEmail(user.getEmail());
        return AuthDto.AuthResponse.builder()
            .token(token)
            .type("Bearer")
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getGlobalRole())
            .build();
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findByEmail(userDetails.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

        return AuthDto.AuthResponse.builder()
            .token(jwt)
            .type("Bearer")
            .id(userDetails.getId())
            .name(userDetails.getName())
            .email(userDetails.getEmail())
            .role(user.getGlobalRole())
            .build();
    }

    public List<AuthDto.UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(u -> new AuthDto.UserResponse(u.getId(), u.getName(), u.getEmail(), u.getGlobalRole()))
            .collect(Collectors.toList());
    }

    public User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl)
            SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
