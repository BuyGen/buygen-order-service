package com.project.microservices.order_service.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface InventoryClient {

    Logger log = LoggerFactory.getLogger(InventoryClient.class);

    @GetExchange("/api/inventory")
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @Retry(name = "inventory")
    Boolean isInStock(
            @RequestParam("skuCode") String skuCode,
            @RequestParam("quantity") Integer quantity
    );

    default Boolean fallbackMethod(String skuCode, Integer quantity, Throwable t) {
        log.warn("Inventory service unavailable for skuCode={}, reason={}", skuCode, t.getMessage());
        return false;
    }
}
