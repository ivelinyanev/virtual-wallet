package example.backend.repositories;

import example.backend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(
            "SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username"
    )
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByUsernameOrEmailOrPhoneNumber(String username, String email, String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("""
                SELECT u FROM User u
                WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR u.phoneNumber LIKE CONCAT('%', :query, '%')
            """)
    Page<User> search(String query, Pageable pageable);
}
