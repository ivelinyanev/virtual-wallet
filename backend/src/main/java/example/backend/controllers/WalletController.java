package example.backend.controllers;

import example.backend.dtos.wallet.PrivateWalletDto;
import example.backend.dtos.wallet.TopUpRequest;
import example.backend.dtos.wallet.WalletCreateReq;
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
@RequestMapping("api/v0/wallets")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{id}")
    public ResponseEntity<PrivateWalletDto> getById(@PathVariable Long id) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(WalletMapper.toPrivateWalletDto(walletService.getWalletById(id)));
    }

    @GetMapping
    public ResponseEntity<List<PrivateWalletDto>> getMyWallets() {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        walletService
                                .getMyWallets()
                                .stream()
                                .map(WalletMapper::toPrivateWalletDto)
                                .toList()
                );
    }

    @PostMapping
    public ResponseEntity<PrivateWalletDto> create(@RequestBody @Valid WalletCreateReq request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        WalletMapper.toPrivateWalletDto(
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
}
