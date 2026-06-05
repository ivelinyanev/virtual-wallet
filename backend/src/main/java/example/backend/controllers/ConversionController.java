package example.backend.controllers;

import example.backend.enums.Currency;
import example.backend.services.protocols.ConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v0/conversions")
public class ConversionController {

    private final ConversionService conversionService;

    @GetMapping("/rate")
    public ResponseEntity<Map<String, BigDecimal>> getRate(
            @RequestParam String from,
            @RequestParam String to
    ) {
        BigDecimal rate = conversionService.getRate(Currency.valueOf(from.toUpperCase()), Currency.valueOf(to.toUpperCase()));
        return ResponseEntity.ok(Map.of("rate", rate));
    }
}
