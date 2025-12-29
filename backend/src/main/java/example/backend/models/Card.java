package example.backend.models;

import example.backend.enums.CardBrand;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "cards",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "fingerprint", "user_id"
                })
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "card_id")
    private Long id;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "fingerprint", nullable = false, unique = true)
    private String fingerprint;

    @Column(name = "brand")
    @Enumerated(EnumType.STRING)
    private CardBrand cardBrand;

    @Column(name = "last4", nullable = false)
    private String last4;

    @Column(name = "expiration_month", nullable = false)
    private int expirationMonth;

    @Column(name = "expiration_year", nullable = false)
    private int expirationYear;

    @Transient
    private String cvv;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User cardHolder;

    @Transient
    public String getMaskedNumber() {
        return "**** **** **** " + (last4 == null ? "????" : last4);
    }
}
