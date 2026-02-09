package example.backend.mappers;

import example.backend.dtos.wallet.PrivateWalletDto;
import example.backend.dtos.wallet.WalletCreateReq;
import example.backend.models.Wallet;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public Wallet toWallet(WalletCreateReq request) {
        Wallet wallet = new Wallet();

        wallet.setName(request.name());
        wallet.setCurrency(request.currency());

        return wallet;
    }

    public PrivateWalletDto toPrivateWalletDto(Wallet wallet) {

        return new PrivateWalletDto(
                wallet.getId(),
                wallet.getOwner().getFirstName() + " " + wallet.getOwner().getLastName(),
                wallet.getName(),
                wallet.getBalance(),
                wallet.getCurrency()
        );
    }
}
