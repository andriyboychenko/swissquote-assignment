package com.example.swissquote.application.activity;

import com.example.swissquote.application.customer.CustomerLookupRepository;
import com.example.swissquote.domain.activity.ActivityType;
import com.example.swissquote.domain.activity.CustomerActivity;
import com.example.swissquote.domain.activity.CustomerActivityPage;
import com.example.swissquote.domain.activity.CustomerActivityReport;
import com.example.swissquote.domain.activity.CustomerActivitySearchCriteria;
import com.example.swissquote.domain.activity.CustomerActivitySummary;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerActivityServiceTests {

    private final CustomerActivityRepository repository = mock(CustomerActivityRepository.class);
    private final CustomerLookupRepository customerLookupRepository = mock(CustomerLookupRepository.class);
    private final CustomerActivityService service = new CustomerActivityService(repository, customerLookupRepository);

    @Test
    void getActivityReportReturnsReportForExistingCustomer() {
        UUID customerId = UUID.randomUUID();
        CustomerActivityReport report = new CustomerActivityReport(
                customerId,
                new CustomerActivitySummary(1, 1, 0, 0, 0, 0),
                List.of(new CustomerActivity(
                        UUID.randomUUID(),
                        ActivityType.CARD,
                        BigDecimal.valueOf(125.50),
                        "CHF",
                        "Completed",
                        Instant.parse("2026-09-04T12:00:00Z"),
                        "Merchant 001",
                        "Credit",
                        "PAN ****1234, MCC 5411",
                        List.of()
                )),
                new CustomerActivityPage(100, 0, 1, false, 1)
        );
        when(customerLookupRepository.existsById(customerId)).thenReturn(true);
        when(repository.findActivityReport(customerId, 50, 0, CustomerActivitySearchCriteria.defaultCriteria()))
                .thenReturn(report);

        CustomerActivityReport result = service.getActivityReport(customerId, 50, 0, null);

        assertThat(result).isEqualTo(report);
    }

    @Test
    void getActivityReportThrowsWhenCustomerDoesNotExist() {
        UUID customerId = UUID.randomUUID();
        when(customerLookupRepository.existsById(customerId)).thenReturn(false);

        assertThatThrownBy(() -> service.getActivityReport(customerId, 50, 0, null))
                .isInstanceOf(CustomerActivityNotFoundException.class)
                .hasMessage("Customer activity was not found for customer " + customerId);
        verify(repository, never()).findActivityReport(customerId, 50, 0, CustomerActivitySearchCriteria.defaultCriteria());
    }

    @Test
    void getActivityReportRequiresCustomerId() {
        assertThatNullPointerException()
                .isThrownBy(() -> service.getActivityReport(null, 50, 0, null))
                .withMessage("customerId must not be null");
    }

    @Test
    void getActivityReportBoundsPagination() {
        UUID customerId = UUID.randomUUID();
        CustomerActivityReport report = new CustomerActivityReport(
                customerId,
                new CustomerActivitySummary(0, 0, 0, 0, 0, 0),
                List.of(),
                new CustomerActivityPage(100, 0, 0, false, 0)
        );
        when(customerLookupRepository.existsById(customerId)).thenReturn(true);
        when(repository.findActivityReport(customerId, 100, 0, CustomerActivitySearchCriteria.defaultCriteria()))
                .thenReturn(report);

        CustomerActivityReport result = service.getActivityReport(customerId, 500, -10, null);

        assertThat(result).isEqualTo(report);
        verify(repository).findActivityReport(customerId, 100, 0, CustomerActivitySearchCriteria.defaultCriteria());
    }
}
