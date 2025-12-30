package example.backend.repositories;

import example.backend.models.Transaction;
import example.backend.models.User;
import example.backend.models.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByWallet_Owner(User owner);

}
