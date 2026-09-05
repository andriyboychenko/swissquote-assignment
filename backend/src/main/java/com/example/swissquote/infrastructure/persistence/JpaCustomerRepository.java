package com.example.swissquote.infrastructure.persistence;

import com.example.swissquote.application.customer.CustomerLookupRepository;
import com.example.swissquote.application.customer.CustomerSearchRepository;
import com.example.swissquote.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaCustomerRepository
        extends JpaRepository<CustomerEntity, UUID>, CustomerLookupRepository, CustomerSearchRepository {

    @Query(value = """
            SELECT customer_id
            FROM customers
            WHERE customer_id::TEXT LIKE CONCAT(:query, '%')
            ORDER BY customer_id
            LIMIT :limit
            """, nativeQuery = true)
    @Override
    List<UUID> findCustomerIds(@Param("query") String query, @Param("limit") int limit);
}
