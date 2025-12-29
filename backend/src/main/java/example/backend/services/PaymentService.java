package example.backend.services;

import example.backend.dtos.CardCreateReq;
import example.backend.dtos.CardMetaData;

public interface PaymentService {

    CardMetaData tokenize(CardCreateReq request);
}
