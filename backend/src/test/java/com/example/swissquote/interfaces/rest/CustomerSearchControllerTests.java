package com.example.swissquote.interfaces.rest;

import com.example.swissquote.application.customer.CustomerSearchService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerSearchControllerTests {

    @Test
    void customerSuggestionsReturnsMappedCustomerIds() {
        UUID firstCustomerId = UUID.randomUUID();
        UUID secondCustomerId = UUID.randomUUID();
        CustomerSearchService searchService = mock(CustomerSearchService.class);
        when(searchService.findCustomerIds("abc", 8)).thenReturn(List.of(firstCustomerId, secondCustomerId));
        CustomerSearchController controller = new CustomerSearchController(searchService);

        List<CustomerSuggestionResponse> response = controller.customerSuggestions("abc", 8);

        assertThat(response)
                .extracting(CustomerSuggestionResponse::customerId)
                .containsExactly(firstCustomerId.toString(), secondCustomerId.toString());
        verify(searchService).findCustomerIds("abc", 8);
    }
}
