package example.backend.controllers;

import example.backend.dtos.user.PrivateUserResponse;
import example.backend.dtos.user.PublicUserResponse;
import example.backend.dtos.user.RegisterRequest;
import example.backend.dtos.user.UpdateUserRequest;
import example.backend.mappers.UserMapper;
import example.backend.services.protocols.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v0/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<PrivateUserResponse> getById(@PathVariable Long userId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(UserMapper.toPrivateUserResponse(userService.getById(userId)));
    }

    @GetMapping
    public ResponseEntity<Page<PrivateUserResponse>> getAll(Pageable pageable) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getAll(pageable).map(UserMapper::toPrivateUserResponse));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PublicUserResponse>> search(@RequestParam("q") String query, Pageable pageable) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.search(query, pageable).map(UserMapper::toPublicUserResponse));
    }

    @PostMapping
    public ResponseEntity<PrivateUserResponse> createByAdmin(@RequestBody @Valid RegisterRequest registerUserDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        UserMapper.toPrivateUserResponse(
                                userService.createByAdmin(UserMapper.toUser(registerUserDto))
                        )
                );
    }

    @PutMapping
    public ResponseEntity<PrivateUserResponse> update(@RequestBody @Valid UpdateUserRequest updateUserDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        UserMapper.toPrivateUserResponse(
                                userService.edit(UserMapper.toUser(updateUserDto))
                        )
                );
    }

    @DeleteMapping
    public ResponseEntity<Void> delete() {
        userService.delete();

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
