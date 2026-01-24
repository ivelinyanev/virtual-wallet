package example.backend.services.protocols;

import example.backend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<User> getAll(Pageable pageable);

    Page<User> search(String query, Pageable pageable);

    User getById(Long id);

    User getByUsername(String username);

    User getByEmail(String email);

    User getByPhoneNumber(String phoneNumber);

    User registerUnverified(User user);

    User createByAdmin(User user);

    User edit(User user);

    void delete();

    void block(Long id);

    void unblock(Long id);
}
