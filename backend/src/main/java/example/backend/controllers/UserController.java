package example.backend.controllers;

import example.backend.dtos.user.PrivateUserDto;
import example.backend.dtos.user.PublicUserDto;
import example.backend.dtos.user.RegisterUserDto;
import example.backend.dtos.user.UpdateUserDto;
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

    /*
        TODO: check sorting
     */
    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<PrivateUserDto> getById(@PathVariable Long userId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(UserMapper.toPrivateUserDto(userService.getById(userId)));
    }

    @GetMapping
    public ResponseEntity<Page<PrivateUserDto>> getAll(Pageable pageable) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getAll(pageable).map(UserMapper::toPrivateUserDto));
    }

    @GetMapping("/search/")
    public ResponseEntity<Page<PublicUserDto>> search(@RequestParam("q") String query, Pageable pageable) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.search(query, pageable).map(UserMapper::toPublicUserDto));
    }

    @PostMapping
    public ResponseEntity<PrivateUserDto> createByAdmin(@RequestBody @Valid RegisterUserDto registerUserDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        UserMapper.toPrivateUserDto(
                                userService.createByAdmin(UserMapper.toUser(registerUserDto))
                        )
                );
    }

    @PutMapping
    public ResponseEntity<PrivateUserDto> update(@RequestBody @Valid UpdateUserDto updateUserDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        UserMapper.toPrivateUserDto(
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
