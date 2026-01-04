package example.backend.repositories;

import example.backend.enums.Currency;
import example.backend.models.User;
import example.backend.models.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    List<Wallet> findAllByOwner(User owner);

    boolean existsByCurrencyAndOwner(Currency currency, User owner);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Wallet findByIdForUpdate(@Param("id") Long id);
}
