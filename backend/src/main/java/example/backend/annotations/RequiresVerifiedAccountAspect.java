package example.backend.annotations;

import example.backend.exceptions.AccountNotVerifiedException;
import example.backend.exceptions.UserBlockedException;
import example.backend.models.User;
import example.backend.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import static example.backend.utils.StringConstants.ACCOUNT_NOT_VERIFIED;
import static example.backend.utils.StringConstants.USER_BLOCKED;

@Aspect
@Component
@RequiredArgsConstructor
public class RequiresVerifiedAccountAspect {

    private final AuthUtils authUtils;

    @Before("@annotation(requiresVerifiedAccount)")
    public void confirmAccountVerified(RequiresVerifiedAccount requiresVerifiedAccount) {
        User actingUser = authUtils.getAuthenticatedUser();

        if (actingUser.isBlocked()) {
            throw new UserBlockedException(USER_BLOCKED);
        }

        if (!actingUser.isVerified()) {
            throw new AccountNotVerifiedException(ACCOUNT_NOT_VERIFIED);
        }
    }
}
