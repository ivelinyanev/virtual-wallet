package example.backend.models;

import example.backend.enums.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "wallet")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(name = "name")
    private String name;

    @Column(name = "balance", precision = 2, nullable = false)
    private BigDecimal balance = BigDecimal.valueOf(0);

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", updatable = false, nullable = false)
    private Currency currency;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "wallet")
    Set<Transaction> transactions = new LinkedHashSet<>();
}
