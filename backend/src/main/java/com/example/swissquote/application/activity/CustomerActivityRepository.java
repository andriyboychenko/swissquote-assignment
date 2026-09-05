package com.example.swissquote.application.activity;

import com.example.swissquote.domain.activity.CustomerActivityReport;
import com.example.swissquote.domain.activity.CustomerActivitySearchCriteria;

import java.util.UUID;

public interface CustomerActivityRepository {

    CustomerActivityReport findActivityReport(UUID customerId, int limit, int offset, CustomerActivitySearchCriteria criteria);
}
