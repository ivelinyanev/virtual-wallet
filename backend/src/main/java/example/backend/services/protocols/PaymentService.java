package example.backend.services.protocols;

import example.backend.dtos.card.CardCreateRequest;
import example.backend.dtos.card.CardTokenizationResult;

import java.math.BigDecimal;

public interface PaymentService {

    CardTokenizationResult tokenize(CardCreateRequest request);

    void charge(String token, BigDecimal amount);

    void withdraw(String token, BigDecimal amount);
}
