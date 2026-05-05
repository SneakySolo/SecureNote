package com.SneakySolo.SecureNote.Service;

import com.SneakySolo.SecureNote.Dto.AuthResponseDTO;
import com.SneakySolo.SecureNote.Dto.LoginRequestDTO;
import com.SneakySolo.SecureNote.Dto.RegisterRequestDTO;
import com.SneakySolo.SecureNote.Entity.Role;
import com.SneakySolo.SecureNote.Entity.User;
import com.SneakySolo.SecureNote.Repository.UserRepository;
import com.SneakySolo.SecureNote.Util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public String register(RegisterRequestDTO dto) {

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username is already in use");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);

        return "Successfully registered";
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {

        // entry point for authorized request from SecurityFilterChain,
        // we pass a token that has username and password to AuthManager that figures out which manager to choose
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));

        // if AuthProvider returns not valid then error as 401
        if (!authentication.isAuthenticated()) {
            throw new RuntimeException("Invalid username and password");
        }

        String token = jwtUtil.generateToken(dto.getUsername());

        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        return response;
    }
}
