package com.project.microservices.order_service.service;

import com.project.microservices.order_service.client.InventoryClient;
import com.project.microservices.order_service.dto.OrderRequest;
import com.project.microservices.order.event.OrderPlacedEvent;
import com.project.microservices.order_service.model.Order;
import com.project.microservices.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;




@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public void placeOrder(OrderRequest orderRequest) {


        var isProductInStock = inventoryClient.isInStock(orderRequest.skuCode(), orderRequest.quantity());

        //Map OrderRequest to Order object
        if(isProductInStock) {
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setPrice(orderRequest.price().multiply(BigDecimal.valueOf(orderRequest.quantity())));
            order.setSkuCode(orderRequest.skuCode());
            order.setQuantity(orderRequest.quantity());

            //Save order to OrderRepository
            orderRepository.save(order);
            //send the message to kafka topic
            //order number and email
            OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent();
            orderPlacedEvent.setOrderNumber(order.getOrderNumber());
            orderPlacedEvent.setEmail(orderRequest.userDetails().email());
            orderPlacedEvent.setFirstName(
                    orderRequest.userDetails().firstName() == null ? "" : orderRequest.userDetails().firstName()
            );

            orderPlacedEvent.setLastName(
                    orderRequest.userDetails().lastName() == null ? "" : orderRequest.userDetails().lastName()
            );


            log.info("Start - Sending OrderPlacedEvent to Kafka Topic order-placed", orderPlacedEvent);
            kafkaTemplate.send("order-placed", orderPlacedEvent);
            log.info("End - Sending OrderPlacedEvent to Kafka Topic order-placed");
        }
        else {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product with skuCode " + orderRequest.skuCode() + " is not in stock"
            );

        }
    }
}
