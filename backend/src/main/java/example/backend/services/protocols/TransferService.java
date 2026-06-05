package example.backend.services.protocols;

import example.backend.dtos.transfer.OwnWalletTransferRequest;
import example.backend.dtos.transfer.TransferRequest;

public interface TransferService {

    void transfer(TransferRequest request);

    void internalTransfer(OwnWalletTransferRequest request);

}
