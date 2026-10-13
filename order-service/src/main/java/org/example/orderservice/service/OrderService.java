package org.example.orderservice.service;

import org.example.orderservice.dto.request.OrderRequestDTO;
import org.example.orderservice.dto.response.OrderResponseDTO;

import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(OrderRequestDTO requestDTO);
    OrderResponseDTO getOrderById(Long id);
    List<OrderResponseDTO> getAllOrders();
}
