package example.backend.annotations;

import example.backend.services.protocols.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class RequiresVerifiedAccountAspect {

    private final AuthorizationService authorizationService;

    @Before("@annotation(requiresVerifiedAccount)")
    public void confirmAccountVerified(RequiresVerifiedAccount requiresVerifiedAccount) {
        authorizationService.assertVerifiedAndActive();
    }
}
