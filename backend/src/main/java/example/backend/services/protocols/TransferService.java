package example.backend.services.protocols;

import example.backend.dtos.transfer.OwnWalletTransferReq;
import example.backend.dtos.transfer.TransferReq;

public interface TransferService {

    void transfer(TransferReq request);

    void internalTransfer(OwnWalletTransferReq request);

}
