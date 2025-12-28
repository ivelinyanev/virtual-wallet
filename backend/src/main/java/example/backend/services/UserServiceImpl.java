package example.backend.services;

import example.backend.enums.ERole;
import example.backend.exceptions.DuplicateException;
import example.backend.exceptions.EntityNotFoundException;
import example.backend.models.Role;
import example.backend.models.User;
import example.backend.repositories.RoleRepository;
import example.backend.repositories.UserRepository;
import example.backend.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static example.backend.utils.StringConstants.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtils authUtils;

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", "id", String.valueOf(id)));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User", "username", username));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User", "email", email));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public User getByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new EntityNotFoundException("User", "phone number", phoneNumber));
    }

    @Override
    @Transactional
    public User create(User user) {
        if (existsByUsernameOrEmailOrPhoneNumber(user.getUsername(), user.getEmail(), user.getPhoneNumber())) {
            throw new DuplicateException(USER_WITH_USERNAME_EMAIL_NUMBER_ALREADY_EXISTS);
        }

        Role roleUser = roleRepository.findByName(ERole.ROLE_USER);
        user.getRoles().add(roleUser);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public User edit(User user) {
        User actingUser = authUtils.getAuthenticatedUser();

        emailPhoneNumberValidation(actingUser, user.getEmail(), user.getPhoneNumber());

        if (user.getPassword() != null) actingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getEmail() != null) actingUser.setEmail(user.getEmail());
        if (user.getPhoneNumber() != null) actingUser.setPhoneNumber(user.getPhoneNumber());
        if (user.getPhotoUrl() != null) actingUser.setPhotoUrl(user.getPhotoUrl());

        return userRepository.save(actingUser);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public void delete() {
        User actingUser = authUtils.getAuthenticatedUser();

        userRepository.delete(actingUser);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void block(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", "id", String.valueOf(id)));

        user.setBlocked(true);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void unblock(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", "id", String.valueOf(id)));

        user.setBlocked(false);
    }

    private boolean existsByUsernameOrEmailOrPhoneNumber(String username, String email, String phoneNumber) {
        return userRepository.existsByUsernameOrEmailOrPhoneNumber(username, email, phoneNumber);
    }

    private void emailPhoneNumberValidation(User actingUser, String newEmail, String newPhoneNumber) {
        if (newEmail != null && !newEmail.equals(actingUser.getEmail())) {
            if (userRepository.existsByEmail(newEmail)) {
                throw new DuplicateException(EMAIL_ALREADY_IN_USE);
            }
        }

        if (newPhoneNumber != null && !newPhoneNumber.equals(actingUser.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(newPhoneNumber)) {
                throw new DuplicateException(PHONE_NUMBER_ALREADY_IN_USE);
            }
        }
    }
}
