package example.backend.dtos.card;

public record CardCreateRequest(
        String cardNumber,
        String firstName,
        String lastName,
        Integer expMonth,
        Integer expYear,
        String cvv
) {
}
