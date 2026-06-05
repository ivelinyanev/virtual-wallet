package example.backend.controllers;

import example.backend.dtos.wallet.WalletResponse;
import example.backend.dtos.wallet.TopUpRequest;
import example.backend.dtos.wallet.WalletCreateRequest;
import example.backend.dtos.wallet.WithdrawRequest;
import example.backend.mappers.WalletMapper;
import example.backend.services.protocols.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v0/wallets")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> getById(@PathVariable Long id) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(WalletMapper.toWalletResponse(walletService.getWalletById(id)));
    }

    @GetMapping
    public ResponseEntity<List<WalletResponse>> getMyWallets() {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        walletService
                                .getMyWallets()
                                .stream()
                                .map(WalletMapper::toWalletResponse)
                                .toList()
                );
    }

    @PostMapping
    public ResponseEntity<WalletResponse> create(@RequestBody @Valid WalletCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        WalletMapper.toWalletResponse(
                                walletService.createWallet(WalletMapper.toWallet(request))
                        )
                );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        walletService.deleteWallet(id);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PostMapping("/top-up")
    public ResponseEntity<?> topUp(@RequestBody @Valid TopUpRequest request) {
        walletService.topUp(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody @Valid WithdrawRequest request) {
        walletService.withdraw(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
