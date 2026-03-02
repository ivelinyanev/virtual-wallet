package example.backend.controllers;

import example.backend.dtos.filters.TransactionFilterRequest;
import example.backend.dtos.transaction.TransactionResponse;
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
@RequestMapping("api/v0/transactions")
@RequiredArgsConstructor
public class TransactionController {

    public TransactionService transactionService;

    @GetMapping("/all")
    ResponseEntity<Page<TransactionResponse>> getAllTransactions(
            @RequestParam(required = false) TransactionFilterRequest request,
            @RequestParam(required = false) Pageable pageable
    ) {
        Page<Transaction> page = transactionService.getAllTransactions(request, pageable);

        Page<TransactionResponse> response = page.map(TransactionMapper::toTransactionResponse);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping
    ResponseEntity<Page<TransactionResponse>> getMyTransactions(
            @RequestParam(required = false) TransactionFilterRequest request,
            @RequestParam(required = false) Pageable pageable
    ) {
        Page<Transaction> page = transactionService.getMyTransactions(request, pageable);

        Page<TransactionResponse> response = page.map(TransactionMapper::toTransactionResponse);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/{transactionId}")
    ResponseEntity<TransactionResponse> getMyTransactionById(@PathVariable Long transactionId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        TransactionMapper.toTransactionResponse(transactionService.getMyTransactionById(transactionId))
                );
    }

    @GetMapping("/admin/{transactionId}")
    ResponseEntity<TransactionResponse> getAnyTransactionById(@PathVariable Long transactionId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        TransactionMapper.toTransactionResponse(transactionService.getMyTransactionById(transactionId))
                );
    }
}
