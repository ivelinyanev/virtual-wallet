package example.backend.repositories;

import example.backend.models.Card;
import example.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findAllByCardHolderAndDeletedFalse(User cardHolder);

    List<Card> findAllByCardHolderIdAndDeletedFalse(Long id);

    Optional<Card> findByIdAndDeletedFalse(Long id);

    boolean existsByFingerprintAndCardHolderAndDeletedFalse(String fingerprint, User cardHolder);
}
