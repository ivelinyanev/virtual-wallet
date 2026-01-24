package example.backend.controllers;

import example.backend.dtos.user.LoginUserDto;
import example.backend.dtos.user.RegisterUserDto;
import example.backend.dtos.user.PrivateUserDto;
import example.backend.dtos.user.VerifyUserDto;
import example.backend.mappers.UserMapper;
import example.backend.services.protocols.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static example.backend.utils.StringConstants.CONFIRMATION_EMAIL_SENT;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginUserDto loginUserDto) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(authService.login(loginUserDto));
    }

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

    @GetMapping("/me")
    public ResponseEntity<PrivateUserDto> getMe() {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userMapper.toPrivateUserDto(authService.getMe()));
    }
}
