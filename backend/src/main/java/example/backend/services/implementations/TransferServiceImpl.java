package example.backend.services.implementations;

import example.backend.enums.TransactionType;
import example.backend.exceptions.EntityNotFoundException;
import example.backend.exceptions.ImpossibleOperationException;
import example.backend.models.Wallet;
import example.backend.repositories.WalletRepository;
import example.backend.services.protocols.ConversionService;
import example.backend.services.protocols.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static example.backend.utils.StringConstants.*;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final WalletRepository walletRepository;
    private final TransactionServiceImpl transactionService;
    private final ConversionService conversionService;

    @Override
    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        if (fromId.equals(toId)) {
            throw new ImpossibleOperationException(SAME_WALLET_TRANSACTION_IMPOSSIBLE);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ImpossibleOperationException(TRANSFER_AMOUNT_CANNOT_BE_NEGATIVE);
        }

        Wallet from = walletRepository.findByIdForUpdate(fromId);
        Wallet to = walletRepository.findByIdForUpdate(toId);

        if (from == null || to == null) {
            throw new EntityNotFoundException(WALLET_IDS_NOT_FOUND);
        }

        if (from.getBalance().compareTo(amount) < 0) {
            throw new ImpossibleOperationException(INSUFFICIENT_FUNDS);
        }

        from.setBalance(from.getBalance().subtract(amount));

        /*
            If wallet currencies differ, convert and work with the converted amount
         */
        if (!from.getCurrency().equals(to.getCurrency())) {
            BigDecimal convertedAmount = conversionService.convert(
                    from.getCurrency(),
                    to.getCurrency(),
                    amount
            );

            to.setBalance(to.getBalance().add(convertedAmount));

            transactionService.recordTransaction(
                    to,
                    from,
                    convertedAmount,
                    TransactionType.TRANSFER_IN
            );
        } else {
            to.setBalance(to.getBalance().add(amount));

            transactionService.recordTransaction(
                    to,
                    from,
                    amount,
                    TransactionType.TRANSFER_IN
            );
        }

        transactionService.recordTransaction(
                from,
                to,
                amount.negate(),
                TransactionType.TRANSFER_OUT
        );
    }
}
