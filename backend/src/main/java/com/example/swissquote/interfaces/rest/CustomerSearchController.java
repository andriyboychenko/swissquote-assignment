package com.example.swissquote.interfaces.rest;

import com.example.swissquote.application.customer.CustomerSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerSearchController {

    private final CustomerSearchService customerSearchService;

    public CustomerSearchController(CustomerSearchService customerSearchService) {
        this.customerSearchService = customerSearchService;
    }

    @GetMapping
    public List<CustomerSuggestionResponse> customerSuggestions(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "8") int limit
    ) {
        return customerSearchService.findCustomerIds(query, limit)
                .stream()
                .map(CustomerSuggestionResponse::fromCustomerId)
                .toList();
    }
}
