package com.example.swissquote.interfaces.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@ConditionalOnProperty(prefix = "app.auth.mock", name = "enabled", havingValue = "true")
public class MockLoginController {

    private static final String DEFAULT_OPERATOR = "analyst-one";
    private static final Map<String, String> OPERATORS = Map.of(
            "analyst-one", "Sarah Connor",
            "analyst-two", "Lisbeth Salander",
            "risk-reviewer", "John McClane"
    );

    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @GetMapping("/mock-login")
    public String login(
            @RequestParam(defaultValue = DEFAULT_OPERATOR) String operator,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String requestedOperator = StringUtils.hasText(operator) ? operator : DEFAULT_OPERATOR;
        String normalizedOperator = OPERATORS.containsKey(requestedOperator) ? requestedOperator : DEFAULT_OPERATOR;
        String displayName = OPERATORS.get(normalizedOperator);
        MockOperatorPrincipal principal = new MockOperatorPrincipal(normalizedOperator, displayName);
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal,
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, request, response);

        return "redirect:/";
    }
}
