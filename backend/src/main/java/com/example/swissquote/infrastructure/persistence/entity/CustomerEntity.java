package com.example.swissquote.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "customers")
public class CustomerEntity {

    @Id
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    protected CustomerEntity() {
    }

    public CustomerEntity(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID customerId() {
        return customerId;
    }
}
