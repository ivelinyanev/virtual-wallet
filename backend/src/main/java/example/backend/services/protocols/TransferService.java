package example.backend.services.protocols;

import java.math.BigDecimal;

public interface TransferService {

    void transfer(Long fromId, Long toId, BigDecimal amount);

}
