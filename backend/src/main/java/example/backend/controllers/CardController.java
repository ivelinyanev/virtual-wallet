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
    private final CardMapper cardMapper;

    /*
        TODO: Must be pageable + needs search
     */
    @GetMapping("/all")
    public ResponseEntity<List<PrivateCardDto>> getCards() {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        cardService
                                .getCards()
                                .stream()
                                .map(cardMapper::toPrivateCardDto)
                                .toList()
                );
    }

    /*
        TODO: Must be pageable + needs search
     */
    @GetMapping
    public ResponseEntity<List<PrivateCardDto>> getMyCards() {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        cardService
                                .getMyCards()
                                .stream()
                                .map(cardMapper::toPrivateCardDto)
                                .toList()
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrivateCardDto> getCardById(@PathVariable Long id) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(cardMapper.toPrivateCardDto(cardService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<PrivateCardDto> create(@RequestBody @Valid CardCreateReq request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cardMapper.toPrivateCardDto(cardService.create(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cardService.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}
