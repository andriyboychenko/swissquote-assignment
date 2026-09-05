package com.example.swissquote.interfaces.rest;

import com.example.swissquote.application.activity.CustomerActivityService;
import com.example.swissquote.domain.activity.ActivityType;
import com.example.swissquote.domain.activity.CustomerActivityFilter;
import com.example.swissquote.domain.activity.CustomerActivitySearchCriteria;
import com.example.swissquote.domain.activity.CustomerActivitySort;
import com.example.swissquote.domain.activity.SortDirection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerActivityController {

    private final CustomerActivityService customerActivityService;

    public CustomerActivityController(CustomerActivityService customerActivityService) {
        this.customerActivityService = customerActivityService;
    }

    @GetMapping("/{customerId}/activities")
    public CustomerActivityReportResponse customerActivities(
            @PathVariable UUID customerId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) Instant createdFrom,
            @RequestParam(required = false) Instant createdTo,
            @RequestParam(required = false) ActivityType activityType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal amountMin,
            @RequestParam(required = false) BigDecimal amountMax,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String counterparty,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String detail,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") SortDirection sortDirection
    ) {
        CustomerActivitySearchCriteria criteria = new CustomerActivitySearchCriteria(
                new CustomerActivityFilter(
                        createdFrom,
                        createdTo,
                        activityType,
                        status,
                        amountMin,
                        amountMax,
                        currency,
                        counterparty,
                        channel,
                        detail
                ),
                new CustomerActivitySort(sortBy, sortDirection)
        );
        return CustomerActivityReportResponse.fromDomain(
                customerActivityService.getActivityReport(customerId, limit, offset, criteria)
        );
    }
}
