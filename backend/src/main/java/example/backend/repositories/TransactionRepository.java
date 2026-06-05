package example.backend.repositories;

import example.backend.models.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository
        extends JpaRepository<Transaction, Long>,
                JpaSpecificationExecutor<Transaction> {

    @Override
    @EntityGraph(attributePaths = {
        "wallet", "wallet.owner",
        "counterpartyWallet", "counterpartyWallet.owner",
        "card"
    })
    @NonNull
    Page<Transaction> findAll(Specification<Transaction> spec, @NonNull Pageable pageable);

}
