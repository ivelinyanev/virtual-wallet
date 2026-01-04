package example.backend.services.protocols;

import example.backend.dtos.card.CardCreateReq;
import example.backend.dtos.card.CardMetaData;

public interface PaymentService {

    CardMetaData tokenize(CardCreateReq request);
}
