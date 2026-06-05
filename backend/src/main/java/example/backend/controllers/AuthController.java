package example.backend.controllers;

import example.backend.dtos.user.*;
import example.backend.mappers.UserMapper;
import example.backend.services.protocols.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static example.backend.utils.StringConstants.CONFIRMATION_EMAIL_SENT;

@RestController
@RequestMapping("/api/v0/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginUserDto) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(authService.login(loginUserDto));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequest dto) {
        authService.register(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(String.format(CONFIRMATION_EMAIL_SENT, dto.email()));

    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody @Valid VerifyRequest dto) {
        authService.verifyAccount(dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<PrivateUserResponse> getMe() {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(UserMapper.toPrivateUserResponse(authService.getMe()));
    }
}
