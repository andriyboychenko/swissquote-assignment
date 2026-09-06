package com.example.swissquote.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import static org.assertj.core.api.Assertions.assertThat;

class MockLoginControllerTests {

    private final MockLoginController controller = new MockLoginController();

    @Test
    void loginStoresMockOperatorAuthenticationInSession() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String viewName = controller.login("analyst-two", request, response);

        SecurityContext securityContext = (SecurityContext) request.getSession().getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
        );
        MockOperatorPrincipal principal = (MockOperatorPrincipal) securityContext.getAuthentication().getPrincipal();

        assertThat(viewName).isEqualTo("redirect:/");
        assertThat(securityContext.getAuthentication().isAuthenticated()).isTrue();
        assertThat(principal.subject()).isEqualTo("analyst-two");
        assertThat(principal.displayName()).isEqualTo("Lisbeth Salander");
    }

    @Test
    void loginFallsBackToDefaultOperatorForUnknownValue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.login("unknown", request, response);

        SecurityContext securityContext = (SecurityContext) request.getSession().getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
        );
        MockOperatorPrincipal principal = (MockOperatorPrincipal) securityContext.getAuthentication().getPrincipal();

        assertThat(principal.subject()).isEqualTo("analyst-one");
        assertThat(principal.displayName()).isEqualTo("Sarah Connor");
    }
}
