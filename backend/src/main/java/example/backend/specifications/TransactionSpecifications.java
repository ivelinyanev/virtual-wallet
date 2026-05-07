package example.backend.specifications;

import example.backend.dtos.filters.TransactionFilterRequest;
import example.backend.enums.TransactionStatus;
import example.backend.enums.TransactionType;
import example.backend.models.Transaction;
import example.backend.models.User;
import example.backend.models.Wallet;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class TransactionSpecifications {

    public static Specification<Transaction> buildSpec(User actingUser, TransactionFilterRequest request) {

        Specification<Transaction> spec = Specification.where(null);

        if (actingUser != null) {
            spec = spec.and(TransactionSpecifications.belongsToUser(actingUser));
        }

        if (actingUser == null) {
            spec = spec.and(TransactionSpecifications.hasUserId(request.targetUserId()));
        }

        spec = spec.and(TransactionSpecifications.hasTransactionId(request.transactionId()))
                .and(TransactionSpecifications.hasStatus(request.status()))
                .and(TransactionSpecifications.hasType(request.type()))
                .and(TransactionSpecifications.createdAfter(request.from()))
                .and(TransactionSpecifications.createdBefore(request.to()));

        return spec;
    }

    public static Specification<Transaction> hasUserId(Long userId) {
        return (root, query, cb) -> {
            if (userId == null) return null;

            Join<Transaction, Wallet> walletJoin = root.join("wallet");

            return cb.equal(walletJoin.get("owner").get("id"), userId);
        };
    }

    public static Specification<Transaction> hasTransactionId(Long id) {
        return (root, query, cb) ->
                id == null ? null :
                        cb.equal(root.get("id"), id);
    }

    public static Specification<Transaction> belongsToUser(User user) {
        return (root, query, cb) -> {
            Join<Transaction, Wallet> walletJoin = root.join("wallet");

            return cb.equal(walletJoin.get("owner"), user);
        };
    }

    public static Specification<Transaction> hasStatus(TransactionStatus status) {
        return (root, query, cb) ->
                status == null ? null :
                        cb.equal(root.get("status"), status);
    }

    public static Specification<Transaction> hasType(TransactionType type) {
        return (root, query, cb) ->
                type == null ? null :
                        cb.equal(root.get("type"), type);
    }

    public static Specification<Transaction> createdAfter(LocalDateTime from) {
        return (root, query, cb) ->
                from == null ? null :
                        cb.greaterThanOrEqualTo(root.get("timestamp"), from);
    }

    public static Specification<Transaction> createdBefore(LocalDateTime to) {
        return (root, query, cb) ->
                to == null ? null :
                        cb.lessThanOrEqualTo(root.get("timestamp"), to);
    }

}
