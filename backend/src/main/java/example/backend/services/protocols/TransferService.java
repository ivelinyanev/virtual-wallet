package example.backend.services.protocols;

import example.backend.dtos.transfer.TransferReq;

import java.math.BigDecimal;

public interface TransferService {

    void transfer(TransferReq request);

}
