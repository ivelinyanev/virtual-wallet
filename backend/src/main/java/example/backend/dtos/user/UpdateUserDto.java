package example.backend.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

import static example.backend.utils.StringConstants.*;
import static example.backend.utils.StringConstants.PHONE_NUMBER_MUST_BE_VALID;
import static example.backend.utils.StringConstants.PHOTO_URL_MUST_BE_VALID;

public record UpdateUserDto(
        String password,

        @Email(message = EMAIL_MUST_BE_VALID)
        String email,

        @Pattern(
                regexp = "^\\+[1-9]\\d{7,14}$",
                message = PHONE_NUMBER_MUST_BE_VALID
        )
        String phoneNumber,

        @Pattern(
                regexp = "^(https?://).+$",
                message = PHOTO_URL_MUST_BE_VALID
        )
        String photoUrl
) {
}
