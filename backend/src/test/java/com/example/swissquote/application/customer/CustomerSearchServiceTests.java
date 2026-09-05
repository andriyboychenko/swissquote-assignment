package com.example.swissquote.application.customer;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerSearchServiceTests {

    private final CustomerSearchRepository repository = mock(CustomerSearchRepository.class);
    private final CustomerSearchService service = new CustomerSearchService(repository);

    @Test
    void findCustomerIdsReturnsEmptyListForShortQueries() {
        assertThat(service.findCustomerIds("a", 8)).isEmpty();
        assertThat(service.findCustomerIds(" ", 8)).isEmpty();
    }

    @Test
    void findCustomerIdsTrimsQueryAndBoundsLimit() {
        UUID customerId = UUID.randomUUID();
        when(repository.findCustomerIds("abc", 20)).thenReturn(List.of(customerId));

        List<UUID> customerIds = service.findCustomerIds(" abc ", 40);

        assertThat(customerIds).containsExactly(customerId);
        verify(repository).findCustomerIds("abc", 20);
    }

    @Test
    void findCustomerIdsRequiresQuery() {
        assertThatNullPointerException()
                .isThrownBy(() -> service.findCustomerIds(null, 8))
                .withMessage("query must not be null");
    }
}
