package example.backend.controllers;

import example.backend.dtos.card.CardCreateReq;
import example.backend.dtos.card.PrivateCardDto;
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
    public ResponseEntity<List<PrivateCardDto>> getCardsByUserId(@PathVariable Long userId) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        cardService
                                .getCardsByUserId(userId)
                                .stream()
                                .map(CardMapper::toPrivateCardDto)
                                .toList()
                );
    }

    @GetMapping
    public ResponseEntity<List<PrivateCardDto>> getMyCards() {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        cardService
                                .getMyCards()
                                .stream()
                                .map(CardMapper::toPrivateCardDto)
                                .toList()
                );
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<PrivateCardDto> getCardById(@PathVariable Long cardId) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CardMapper.toPrivateCardDto(cardService.getById(cardId)));
    }

    @PostMapping
    public ResponseEntity<PrivateCardDto> create(@RequestBody @Valid CardCreateReq request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CardMapper.toPrivateCardDto(cardService.create(request)));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> delete(@PathVariable Long cardId) {
        cardService.delete(cardId);

        return ResponseEntity
                .noContent()
                .build();
    }
}
