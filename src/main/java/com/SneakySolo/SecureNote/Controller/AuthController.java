package com.SneakySolo.SecureNote.Controller;

import com.SneakySolo.SecureNote.Dto.AuthResponseDTO;
import com.SneakySolo.SecureNote.Dto.LoginRequestDTO;
import com.SneakySolo.SecureNote.Dto.RegisterRequestDTO;
import com.SneakySolo.SecureNote.Service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequestDTO dto) {
        return authService.register(dto);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        AuthResponseDTO authResponse = authService.login(dto);
        return ResponseEntity.ok(authResponse);
    }
}
