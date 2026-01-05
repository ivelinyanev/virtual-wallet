package example.backend.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import static example.backend.utils.StringConstants.*;

public record VerifyUserDto(
        @NotBlank(message = FIELD_CANNOT_BE_BLANK)
        @Email(message = EMAIL_MUST_BE_VALID)
        String email,

        @NotBlank(message = FIELD_CANNOT_BE_BLANK)
        @Pattern(
                regexp = "^\\d{6}$",
                message = VERIFICATION_CODE_VALIDATION
        )
        String code
) {
}
