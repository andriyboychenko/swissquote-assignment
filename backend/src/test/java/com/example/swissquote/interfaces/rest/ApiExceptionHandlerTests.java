package com.example.swissquote.interfaces.rest;

import com.example.swissquote.application.activity.CustomerActivityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTests {

    @Test
    void handleCustomerActivityNotFoundReturnsProblemDetail() {
        UUID customerId = UUID.randomUUID();
        ApiExceptionHandler handler = new ApiExceptionHandler();

        ProblemDetail problemDetail = handler.handleCustomerActivityNotFound(
                new CustomerActivityNotFoundException(customerId)
        );

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Customer activity not found");
        assertThat(problemDetail.getDetail()).contains(customerId.toString());
        assertThat(problemDetail.getProperties()).containsEntry("customerId", customerId);
    }
}
