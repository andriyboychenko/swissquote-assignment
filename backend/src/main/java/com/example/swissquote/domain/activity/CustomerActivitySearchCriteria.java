package com.example.swissquote.domain.activity;

public record CustomerActivitySearchCriteria(
        CustomerActivityFilter filter,
        CustomerActivitySort sort
) {

    public CustomerActivitySearchCriteria {
        filter = filter == null ? emptyFilter() : filter;
        sort = sort == null ? new CustomerActivitySort("createdAt", SortDirection.DESC) : sort;
    }

    public static CustomerActivitySearchCriteria defaultCriteria() {
        return new CustomerActivitySearchCriteria(emptyFilter(), new CustomerActivitySort("createdAt", SortDirection.DESC));
    }

    private static CustomerActivityFilter emptyFilter() {
        return new CustomerActivityFilter(null, null, null, null, null, null, null, null, null, null);
    }
}
