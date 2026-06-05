package example.backend.controllers;

import example.backend.dtos.card.CardCreateRequest;
import example.backend.dtos.card.CardResponse;
import example.backend.mappers.CardMapper;
import example.backend.services.protocols.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v0/cards")
public class CardController {

    private final CardService cardService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<CardResponse>> getCardsByUserId(@PathVariable Long userId) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        cardService
                                .getCardsByUserId(userId)
                                .stream()
                                .map(CardMapper::toCardResponse)
                                .toList()
                );
    }

    @GetMapping
    public ResponseEntity<List<CardResponse>> getMyCards() {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        cardService
                                .getMyCards()
                                .stream()
                                .map(CardMapper::toCardResponse)
                                .toList()
                );
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CardResponse> getCardById(@PathVariable Long cardId) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CardMapper.toCardResponse(cardService.getById(cardId)));
    }

    @PostMapping
    public ResponseEntity<CardResponse> create(@RequestBody @Valid CardCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CardMapper.toCardResponse(cardService.create(request)));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> delete(@PathVariable Long cardId) {
        cardService.delete(cardId);

        return ResponseEntity
                .noContent()
                .build();
    }
}
