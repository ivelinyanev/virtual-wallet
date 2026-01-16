package example.backend.controllers;

import example.backend.dtos.user.RegisterUserDto;
import example.backend.dtos.user.VerifyUserDto;
import example.backend.mappers.UserMapper;
import example.backend.services.protocols.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static example.backend.utils.StringConstants.CONFIRMATION_EMAIL_SENT;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterUserDto dto) {
        authService.register(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(String.format(CONFIRMATION_EMAIL_SENT, dto.email()));

    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody @Valid VerifyUserDto dto) {
        authService.verifyAccount(dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
