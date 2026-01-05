package example.backend.services.protocols;

import example.backend.models.User;

import java.util.List;

public interface UserService {

    List<User> getAll();

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
