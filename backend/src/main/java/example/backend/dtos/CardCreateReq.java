package example.backend.dtos;

public record CardCreateReq(
        String cardNumber,
        String firstName,
        String lastName,
        Integer expMonth,
        Integer expYear,
        String cvv
) {
}
