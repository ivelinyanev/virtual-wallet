package example.backend.controllers;

import example.backend.dtos.filters.TransactionFilterRequest;
import example.backend.dtos.transaction.AdminTransactionResponse;
import example.backend.dtos.transaction.UserTransactionResponse;
import example.backend.mappers.TransactionMapper;
import example.backend.models.Transaction;
import example.backend.services.protocols.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v0/transactions")
@RequiredArgsConstructor
public class TransactionController {

    public final TransactionService transactionService;

    @GetMapping("/all")
    ResponseEntity<Page<AdminTransactionResponse>> getAllTransactions(
            @ModelAttribute TransactionFilterRequest request,
            Pageable pageable
    ) {
        pageable = pageable == null ? Pageable.unpaged() : pageable;

        Page<Transaction> page = transactionService.getAllTransactions(request, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(page.map(TransactionMapper::toAdminTransactionResponse));
    }

    @GetMapping
    ResponseEntity<Page<UserTransactionResponse>> getMyTransactions(
            @ModelAttribute TransactionFilterRequest request,
            Pageable pageable
    ) {
        pageable = pageable == null ? Pageable.unpaged() : pageable;

        Page<Transaction> page = transactionService.getMyTransactions(request, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(page.map(TransactionMapper::toUserTransactionResponse));
    }

    @GetMapping("/{transactionId}")
    ResponseEntity<UserTransactionResponse> getMyTransactionById(@PathVariable Long transactionId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(TransactionMapper.toUserTransactionResponse(transactionService.getMyTransactionById(transactionId)));
    }

    @GetMapping("/admin/{transactionId}")
    ResponseEntity<AdminTransactionResponse> getAnyTransactionById(@PathVariable Long transactionId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(TransactionMapper.toAdminTransactionResponse(transactionService.getAnyTransactionById(transactionId)));
    }
}
