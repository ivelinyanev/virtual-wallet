package example.backend.services;

import example.backend.enums.ERole;
import example.backend.exceptions.DuplicateException;
import example.backend.exceptions.EntityNotFoundException;
import example.backend.models.Role;
import example.backend.models.User;
import example.backend.repositories.RoleRepository;
import example.backend.repositories.UserRepository;
import example.backend.services.implementations.UserServiceImpl;
import example.backend.utils.AuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static example.backend.utils.StringConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthUtils authUtils;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@mail.com");
        user.setPhoneNumber("+359888000001");
        user.setPassword("rawPassword");
    }

    // ───────────────────────── getAll ─────────────────────────

    @Test
    void getAll_Should_DelegateToRepository() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<User> result = userService.getAll(Pageable.unpaged());

        assertEquals(1, result.getContent().size());
    }

    // ───────────────────────── search ─────────────────────────

    @Test
    void search_Should_ReturnMatchingUsers() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.search(eq("test"), any(Pageable.class))).thenReturn(page);

        Page<User> result = userService.search("test", Pageable.unpaged());

        assertEquals(1, result.getContent().size());
    }

    // ───────────────────────── getById ─────────────────────────

    @Test
    void getById_Should_ReturnUser_When_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getById(1L);

        assertEquals(user, result);
    }

    @Test
    void getById_Should_Throw_When_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getById(99L));
    }

    // ───────────────────────── getByUsername ─────────────────────────

    @Test
    void getByUsername_Should_ReturnUser_When_Found() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        User result = userService.getByUsername("testuser");

        assertEquals(user, result);
    }

    @Test
    void getByUsername_Should_Throw_When_NotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getByUsername("ghost"));
    }

    // ───────────────────────── getByEmail ─────────────────────────

    @Test
    void getByEmail_Should_ReturnUser_When_Found() {
        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));

        User result = userService.getByEmail("test@mail.com");

        assertEquals(user, result);
    }

    @Test
    void getByEmail_Should_Throw_When_NotFound() {
        when(userRepository.findByEmail("nobody@mail.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getByEmail("nobody@mail.com"));
    }

    // ───────────────────────── getByPhoneNumber ─────────────────────────

    @Test
    void getByPhoneNumber_Should_ReturnUser_When_Found() {
        when(userRepository.findByPhoneNumber("+359888000001")).thenReturn(Optional.of(user));

        User result = userService.getByPhoneNumber("+359888000001");

        assertEquals(user, result);
    }

    @Test
    void getByPhoneNumber_Should_Throw_When_NotFound() {
        when(userRepository.findByPhoneNumber("+00000000000")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.getByPhoneNumber("+00000000000"));
    }

    // ───────────────────────── registerUnverified ─────────────────────────

    @Test
    void registerUnverified_Should_SaveUser_When_Unique() {
        Role roleUser = new Role();
        roleUser.setName(ERole.ROLE_USER);

        when(userRepository.existsByUsernameOrEmailOrPhoneNumber(
                user.getUsername(), user.getEmail(), user.getPhoneNumber()))
                .thenReturn(false);
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(roleUser);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.registerUnverified(user);

        assertEquals(user, result);
        assertEquals("encodedPassword", user.getPassword());
        assertTrue(user.getRoles().contains(roleUser));
        verify(userRepository).save(user);
    }

    @Test
    void registerUnverified_Should_Throw_When_DuplicateExists() {
        when(userRepository.existsByUsernameOrEmailOrPhoneNumber(
                user.getUsername(), user.getEmail(), user.getPhoneNumber()))
                .thenReturn(true);

        DuplicateException ex =
                assertThrows(DuplicateException.class, () -> userService.registerUnverified(user));

        assertEquals(USER_WITH_USERNAME_EMAIL_NUMBER_ALREADY_EXISTS, ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    // ───────────────────────── edit ─────────────────────────

    @Test
    void edit_Should_UpdateEditableFields_When_Valid() {
        User actingUser = new User();
        actingUser.setEmail("old@mail.com");
        actingUser.setPhoneNumber("+359888000001");

        User update = new User();
        update.setEmail("new@mail.com");
        update.setPhoneNumber("+359888000002");
        update.setPassword("newPass");
        update.setPhotoUrl("https://photo.url/img.jpg");

        when(authUtils.getAuthenticatedUser()).thenReturn(actingUser);
        when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("+359888000002")).thenReturn(false);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");
        when(userRepository.save(actingUser)).thenReturn(actingUser);

        userService.edit(update);

        assertEquals("new@mail.com", actingUser.getEmail());
        assertEquals("+359888000002", actingUser.getPhoneNumber());
        assertEquals("encodedNewPass", actingUser.getPassword());
        assertEquals("https://photo.url/img.jpg", actingUser.getPhotoUrl());
    }

    @Test
    void edit_Should_Throw_When_NewEmailAlreadyInUse() {
        User actingUser = new User();
        actingUser.setEmail("old@mail.com");
        actingUser.setPhoneNumber("+359888000001");

        User update = new User();
        update.setEmail("taken@mail.com");

        when(authUtils.getAuthenticatedUser()).thenReturn(actingUser);
        when(userRepository.existsByEmail("taken@mail.com")).thenReturn(true);

        DuplicateException ex =
                assertThrows(DuplicateException.class, () -> userService.edit(update));

        assertEquals(EMAIL_ALREADY_IN_USE, ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void edit_Should_Throw_When_NewPhoneAlreadyInUse() {
        User actingUser = new User();
        actingUser.setEmail("old@mail.com");
        actingUser.setPhoneNumber("+359888000001");

        User update = new User();
        update.setPhoneNumber("+359888999999");

        when(authUtils.getAuthenticatedUser()).thenReturn(actingUser);
        when(userRepository.existsByPhoneNumber("+359888999999")).thenReturn(true);

        DuplicateException ex =
                assertThrows(DuplicateException.class, () -> userService.edit(update));

        assertEquals(PHONE_NUMBER_ALREADY_IN_USE, ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void edit_Should_NotCheckUniqueness_When_SameEmailOrPhoneProvided() {
        User actingUser = new User();
        actingUser.setEmail("same@mail.com");
        actingUser.setPhoneNumber("+359888000001");

        // update contains the same values — no uniqueness check should run
        User update = new User();
        update.setEmail("same@mail.com");
        update.setPhoneNumber("+359888000001");

        when(authUtils.getAuthenticatedUser()).thenReturn(actingUser);
        when(userRepository.save(actingUser)).thenReturn(actingUser);

        userService.edit(update);

        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).existsByPhoneNumber(any());
        verify(userRepository).save(actingUser);
    }

    // ───────────────────────── delete ─────────────────────────

    @Test
    void delete_Should_DeleteAuthenticatedUser() {
        when(authUtils.getAuthenticatedUser()).thenReturn(user);

        userService.delete();

        verify(userRepository).delete(user);
    }

    // ───────────────────────── block ─────────────────────────

    @Test
    void block_Should_SetUserBlocked() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.block(1L);

        assertTrue(user.isBlocked());
        verify(userRepository).save(user);
    }

    @Test
    void block_Should_Throw_When_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.block(99L));
        verify(userRepository, never()).save(any());
    }

    // ───────────────────────── unblock ─────────────────────────

    @Test
    void unblock_Should_SetUserUnblocked() {
        user.setBlocked(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.unblock(1L);

        assertFalse(user.isBlocked());
        verify(userRepository).save(user);
    }

    @Test
    void unblock_Should_Throw_When_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.unblock(99L));
        verify(userRepository, never()).save(any());
    }
}
