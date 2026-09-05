package com.example.swissquote.application.customer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class CustomerSearchService {

    private final CustomerSearchRepository customerSearchRepository;

    public CustomerSearchService(CustomerSearchRepository customerSearchRepository) {
        this.customerSearchRepository = customerSearchRepository;
    }

    @Transactional(readOnly = true)
    public List<UUID> findCustomerIds(String query, int limit) {
        String normalizedQuery = Objects.requireNonNull(query, "query must not be null").trim();

        if (normalizedQuery.length() < 2) {
            return List.of();
        }

        int boundedLimit = Math.max(1, Math.min(limit, 20));
        return customerSearchRepository.findCustomerIds(normalizedQuery, boundedLimit);
    }
}
