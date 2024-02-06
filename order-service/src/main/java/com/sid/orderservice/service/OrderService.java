package com.sid.orderservice.service;

import com.sid.orderservice.dto.InventuryResponse;
import com.sid.orderservice.dto.OrderLineItemsDTO;
import com.sid.orderservice.dto.OrderRequest;
import com.sid.orderservice.model.Order;
import com.sid.orderservice.model.OrderLineItems;
import com.sid.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private  final WebClient webClient;
    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsListDtoList()
                .stream()
                .map(orderLineItemsDTO -> mapToDto(orderLineItemsDTO)).toList();

        order.setOrderLineItemsList(orderLineItems);
        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();
        //Call inventry server and place order if product in stock
        InventuryResponse[] inventuryResponses = webClient.get()
                .uri("http://localhost:8082/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes)
                                .build())
                        .retrieve()
                        .bodyToMono(InventuryResponse[].class)
                        .block();

        boolean allProductsInStock =Arrays.stream(inventuryResponses)
                .allMatch(InventuryResponse::isInStock);

        if (allProductsInStock){
            orderRepository.save(order);
        } else {
            throw  new IllegalArgumentException("Product is not in stock please try later");
        }

    }

    private OrderLineItems mapToDto(OrderLineItemsDTO orderLineItemsDTO) {
        OrderLineItems orderLineItems= new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDTO.getPrice());
        orderLineItems.setQuantity(orderLineItemsDTO.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDTO.getSkuCode());
        return orderLineItems;
    }
}
