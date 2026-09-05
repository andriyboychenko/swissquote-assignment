package com.example.swissquote.interfaces.rest;

import com.example.swissquote.application.activity.CustomerActivityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(CustomerActivityNotFoundException.class)
    public ProblemDetail handleCustomerActivityNotFound(CustomerActivityNotFoundException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
        problemDetail.setTitle("Customer activity not found");
        problemDetail.setProperty("customerId", exception.customerId());
        return problemDetail;
    }
}
