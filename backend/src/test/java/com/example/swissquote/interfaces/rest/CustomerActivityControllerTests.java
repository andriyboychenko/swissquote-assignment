package com.example.swissquote.interfaces.rest;

import com.example.swissquote.application.activity.CustomerActivityService;
import com.example.swissquote.domain.activity.ActivityType;
import com.example.swissquote.domain.activity.CustomerActivity;
import com.example.swissquote.domain.activity.CustomerActivitySearchCriteria;
import com.example.swissquote.domain.activity.CustomerActivityPage;
import com.example.swissquote.domain.activity.CustomerActivityReport;
import com.example.swissquote.domain.activity.CustomerActivitySummary;
import com.example.swissquote.domain.activity.SortDirection;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerActivityControllerTests {

    @Test
    void customerActivitiesReturnsMappedActivityReport() {
        UUID customerId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        CustomerActivityService service = mock(CustomerActivityService.class);
        when(service.getActivityReport(
                org.mockito.ArgumentMatchers.eq(customerId),
                org.mockito.ArgumentMatchers.eq(50),
                org.mockito.ArgumentMatchers.eq(0),
                org.mockito.ArgumentMatchers.any(CustomerActivitySearchCriteria.class)
        )).thenReturn(new CustomerActivityReport(
                customerId,
                new CustomerActivitySummary(1, 1, 0, 0, 0, 0),
                java.util.List.of(new CustomerActivity(
                        transactionId,
                        ActivityType.CARD,
                        BigDecimal.valueOf(125.50),
                        "CHF",
                        "Completed",
                        Instant.parse("2026-09-04T12:00:00Z"),
                        "Merchant 001",
                        "Credit",
                        "PAN ****1234, MCC 5411"
                )),
                new CustomerActivityPage(50, 0, 1, false, 1)
        ));
        CustomerActivityController controller = new CustomerActivityController(service);

        CustomerActivityReportResponse response = controller.customerActivities(
                customerId,
                50,
                0,
                Instant.parse("2026-09-01T00:00:00Z"),
                Instant.parse("2026-09-05T00:00:00Z"),
                ActivityType.CARD,
                "Completed",
                BigDecimal.TEN,
                BigDecimal.valueOf(200),
                "CHF",
                "Merchant",
                "Credit",
                "PAN",
                "amount",
                SortDirection.ASC
        );

        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.summary().totalActivities()).isEqualTo(1);
        assertThat(response.page().limit()).isEqualTo(50);
        assertThat(response.page().hasMore()).isFalse();
        assertThat(response.activities()).hasSize(1);
        assertThat(response.activities().getFirst().transactionId()).isEqualTo(transactionId);
        assertThat(response.activities().getFirst().activityType()).isEqualTo("CARD");
        assertThat(response.activities().getFirst().counterparty()).isEqualTo("Merchant 001");
        ArgumentCaptor<CustomerActivitySearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(CustomerActivitySearchCriteria.class);
        verify(service).getActivityReport(
                org.mockito.ArgumentMatchers.eq(customerId),
                org.mockito.ArgumentMatchers.eq(50),
                org.mockito.ArgumentMatchers.eq(0),
                criteriaCaptor.capture()
        );
        CustomerActivitySearchCriteria criteria = criteriaCaptor.getValue();
        assertThat(criteria.filter().createdFrom()).isEqualTo(Instant.parse("2026-09-01T00:00:00Z"));
        assertThat(criteria.filter().createdTo()).isEqualTo(Instant.parse("2026-09-05T00:00:00Z"));
        assertThat(criteria.filter().activityType()).isEqualTo(ActivityType.CARD);
        assertThat(criteria.filter().status()).isEqualTo("Completed");
        assertThat(criteria.filter().amountMin()).isEqualByComparingTo("10");
        assertThat(criteria.filter().amountMax()).isEqualByComparingTo("200");
        assertThat(criteria.filter().currency()).isEqualTo("CHF");
        assertThat(criteria.filter().counterparty()).isEqualTo("Merchant");
        assertThat(criteria.filter().channel()).isEqualTo("Credit");
        assertThat(criteria.filter().detail()).isEqualTo("PAN");
        assertThat(criteria.sort().sortBy()).isEqualTo("amount");
        assertThat(criteria.sort().direction()).isEqualTo(SortDirection.ASC);
    }

}
