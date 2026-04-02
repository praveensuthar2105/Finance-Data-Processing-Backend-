package com.finance.backend.service;

import com.finance.backend.dto.request.LoginRequest;
import com.finance.backend.dto.request.RefreshTokenRequest;
import com.finance.backend.dto.request.RegisterRequest;
import com.finance.backend.dto.response.AuthResponse;
import com.finance.backend.dto.response.UserResponse;
import com.finance.backend.entity.RefreshToken;
import com.finance.backend.entity.User;
import com.finance.backend.enums.Role;
import com.finance.backend.exception.BadRequestException;
import com.finance.backend.repository.RefreshTokenRepository;
import com.finance.backend.repository.UserRepository;
import com.finance.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Value("${app.jwt.refresh-token-expiry-days}")
    private int refreshTokenExpiryDays;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.VIEWER)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken();

        saveRefreshToken(refreshToken, user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(mapToUserResponse(user))
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BadRequestException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!user.getIsActive()) {
            throw new BadRequestException("User account is disabled");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken();

        saveRefreshToken(refreshToken, user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(mapToUserResponse(user))
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String rawToken = request.getRefreshToken();

        // Find the matching token by iterating all non-revoked tokens
        RefreshToken matchedToken = null;
        List<RefreshToken> allActiveTokens = refreshTokenRepository.findAll()
                .stream()
                .filter(t -> !t.getIsRevoked())
                .toList();

        for (RefreshToken token : allActiveTokens) {
            if (passwordEncoder.matches(rawToken, token.getTokenHash())) {
                matchedToken = token;
                break;
            }
        }

        if (matchedToken == null) {
            throw new BadRequestException("Invalid refresh token");
        }

        if (matchedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            matchedToken.setIsRevoked(true);
            refreshTokenRepository.save(matchedToken);
            throw new BadRequestException("Refresh token has expired");
        }

        // Revoke old token (token rotation)
        matchedToken.setIsRevoked(true);
        refreshTokenRepository.save(matchedToken);

        // Generate new tokens
        User user = matchedToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccessToken = jwtUtil.generateAccessToken(userDetails);
        String newRefreshToken = jwtUtil.generateRefreshToken();

        saveRefreshToken(newRefreshToken, user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .user(mapToUserResponse(user))
                .build();
    }

    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        List<RefreshToken> activeTokens = refreshTokenRepository
                .findByUserIdAndIsRevokedFalse(user.getId());

        activeTokens.forEach(token -> {
            token.setIsRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private void saveRefreshToken(String rawToken, User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(passwordEncoder.encode(rawToken))
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpiryDays))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
