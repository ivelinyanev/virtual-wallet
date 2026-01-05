package example.backend.dtos.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import static example.backend.utils.StringConstants.*;

public record RegisterUserDto(

        @NotBlank(message = FIELD_CANNOT_BE_BLANK)
        @Size(max = 50, message = FIELD_MUST_BE_IN_RANGE)
        String firstName,

        @NotBlank(message = FIELD_CANNOT_BE_BLANK)
        @Size(max = 50, message = FIELD_MUST_BE_IN_RANGE)
        String lastName,

        @NotBlank(message = FIELD_CANNOT_BE_BLANK)
        @Size(max = 50, message = FIELD_MUST_BE_IN_RANGE)
        String username,

        @NotBlank(message = FIELD_CANNOT_BE_BLANK)
        String password,

        @NotBlank(message = FIELD_CANNOT_BE_BLANK)
        @Email(message = EMAIL_MUST_BE_VALID)
        String email,

        @NotBlank(message = FIELD_CANNOT_BE_BLANK)
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