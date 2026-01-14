package com.project.microservices.order_service.service;

import com.project.microservices.order_service.client.InventoryClient;
import com.project.microservices.order_service.dto.OrderRequest;
import com.project.microservices.order_service.model.Order;
import com.project.microservices.order_service.repository.OrderRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    public void placeOrder(OrderRequest orderRequest) {

        var isProductInStock = inventoryClient.isInStock(orderRequest.skuCode(), orderRequest.quantity());

        //Map OrderRequest to Order object
        if(isProductInStock) {
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setPrice(orderRequest.price());
            order.setSkuCode(orderRequest.skuCode());
            order.setQuantity(orderRequest.quantity());

            //Save order to OrderRepository
            orderRepository.save(order);
        }
        else {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product with skuCode " + orderRequest.skuCode() + " is not in stock"
            );

        }
    }
}
