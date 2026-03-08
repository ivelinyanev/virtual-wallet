package example.backend.config;

import example.backend.enums.ERole;
import example.backend.models.Role;
import example.backend.models.User;
import example.backend.repositories.RoleRepository;
import example.backend.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner init(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        return args -> {

            // Insert roles if they don't exist
            if (roleRepository.count() == 0) {
                Role adminRole = new Role();
                adminRole.setName(ERole.ROLE_ADMIN);
                roleRepository.save(adminRole);

                Role userRole = new Role();
                userRole.setName(ERole.ROLE_USER);
                roleRepository.save(userRole);
            }

            // Insert admin, user and unverified if they don't exist
            if (userRepository.count() == 0) {
                Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN);
                Role userRole = roleRepository.findByName(ERole.ROLE_USER);

                User admin = new User();
                admin.setFirstName("Admin");
                admin.setLastName("Admin");
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setEmail("admin@example.com");
                admin.setPhoneNumber("+359432245566");
                admin.setRoles(Set.of(adminRole, userRole));
                admin.setVerified(true);
                userRepository.save(admin);

                User user = new User();
                user.setFirstName("User");
                user.setLastName("User");
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("user"));
                user.setEmail("user@example.com");
                user.setPhoneNumber("+359432245555");
                user.setRoles(Set.of(userRole));
                user.setVerified(true);
                userRepository.save(user);

                User unverified = new User();
                unverified.setFirstName("Unverified");
                unverified.setLastName("Unverified");
                unverified.setUsername("unverified");
                unverified.setPassword(passwordEncoder.encode("unverified"));
                unverified.setEmail("unverified@example.com");
                unverified.setPhoneNumber("+359432242222");
                unverified.setRoles(Set.of(userRole));
                unverified.setVerified(false);
                userRepository.save(unverified);
            }
        };
    }
}
