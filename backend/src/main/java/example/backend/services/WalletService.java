package example.backend.services;

import example.backend.models.Wallet;

import java.util.List;

public interface WalletService {

    List<Wallet> getMyWallets();

    Wallet getWalletById(Long id);

    Wallet createWallet(Wallet wallet);

    void deleteWallet(Long id);
}
