package example.backend.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import static example.backend.utils.StringConstants.EMAIL_MUST_BE_VALID;
import static example.backend.utils.StringConstants.FIELD_CANNOT_BE_BLANK;

public record LoginUserDto(

        @Email(message = EMAIL_MUST_BE_VALID)
        @NotBlank(message = FIELD_CANNOT_BE_BLANK)
        String email,

        @NotBlank(message = FIELD_CANNOT_BE_BLANK)
        String password
) {
}
