package com.example.swissquote.domain.activity;

public record CustomerActivitySort(
        String sortBy,
        SortDirection direction
) {

    public CustomerActivitySort {
        sortBy = sortBy == null || sortBy.isBlank() ? "createdAt" : sortBy;
        direction = direction == null ? SortDirection.DESC : direction;
    }
}
