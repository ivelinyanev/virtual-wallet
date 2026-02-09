package example.backend.services.protocols;

import example.backend.models.User;
import example.backend.models.Wallet;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {

    List<Wallet> getMyWallets();

    Wallet getWalletById(Long id);

    Wallet createWallet(Wallet wallet);

    void createBaseWalletUponVerification(User user);

    void deleteWallet(Long id);

    void topUp(Long walletId, String token, BigDecimal amount);
}
