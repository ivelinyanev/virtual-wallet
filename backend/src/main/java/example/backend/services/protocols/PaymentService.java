package example.backend.services.protocols;

import example.backend.dtos.card.CardCreateReq;
import example.backend.dtos.card.CardMetaData;

import java.math.BigDecimal;

public interface PaymentService {

    CardMetaData tokenize(CardCreateReq request);

    void charge(String token, BigDecimal amount);

    void withdraw(String token, BigDecimal amount);
}
