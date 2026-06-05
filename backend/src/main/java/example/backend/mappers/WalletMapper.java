package example.backend.mappers;

import example.backend.dtos.wallet.WalletResponse;
import example.backend.dtos.wallet.WalletCreateRequest;
import example.backend.models.Wallet;
import org.springframework.stereotype.Component;

public class WalletMapper {

    public static Wallet toWallet(WalletCreateRequest request) {
        Wallet wallet = new Wallet();

        wallet.setName(request.name());
        wallet.setCurrency(request.currency());

        return wallet;
    }

    public static WalletResponse toWalletResponse(Wallet wallet) {

        return new WalletResponse(
                wallet.getId(),
                wallet.getOwner().getFirstName() + " " + wallet.getOwner().getLastName(),
                wallet.getName(),
                wallet.getBalance(),
                wallet.getCurrency()
        );
    }
}
