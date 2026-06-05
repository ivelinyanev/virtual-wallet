package example.backend.controllers;

import example.backend.dtos.transfer.OwnWalletTransferReq;
import example.backend.dtos.transfer.TransferReq;
import example.backend.services.protocols.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v0/transfers")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<?> transfer(@RequestBody @Valid TransferReq request) {
        transferService.transfer(request);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/internal")
    public ResponseEntity<?> internalTransfer(@RequestBody @Valid OwnWalletTransferReq request) {
        transferService.internalTransfer(request);

        return ResponseEntity.ok().build();
    }
}
