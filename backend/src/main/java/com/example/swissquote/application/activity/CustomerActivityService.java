package com.example.swissquote.application.activity;

import com.example.swissquote.application.customer.CustomerLookupRepository;
import com.example.swissquote.domain.activity.CustomerActivityReport;
import com.example.swissquote.domain.activity.CustomerActivitySearchCriteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class CustomerActivityService {

    private final CustomerActivityRepository customerActivityRepository;
    private final CustomerLookupRepository customerLookupRepository;

    public CustomerActivityService(
            CustomerActivityRepository customerActivityRepository,
            CustomerLookupRepository customerLookupRepository
    ) {
        this.customerActivityRepository = customerActivityRepository;
        this.customerLookupRepository = customerLookupRepository;
    }

    @Transactional(readOnly = true)
    public CustomerActivityReport getActivityReport(
            UUID customerId,
            int limit,
            int offset,
            CustomerActivitySearchCriteria criteria
    ) {
        Objects.requireNonNull(customerId, "customerId must not be null");

        int boundedLimit = Math.max(1, Math.min(limit, 100));
        int boundedOffset = Math.max(offset, 0);

        if (!customerLookupRepository.existsById(customerId)) {
            throw new CustomerActivityNotFoundException(customerId);
        }

        CustomerActivitySearchCriteria safeCriteria = criteria == null
                ? CustomerActivitySearchCriteria.defaultCriteria()
                : criteria;
        return customerActivityRepository.findActivityReport(customerId, boundedLimit, boundedOffset, safeCriteria);
    }
}
