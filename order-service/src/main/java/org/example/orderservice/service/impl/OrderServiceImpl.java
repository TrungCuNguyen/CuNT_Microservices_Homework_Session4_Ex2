package org.example.orderservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.constant.OrderConstant;
import org.example.orderservice.dto.request.OrderRequestDTO;
import org.example.orderservice.dto.response.OrderResponseDTO;
import org.example.orderservice.dto.response.ProductClientResponseDTO;
import org.example.orderservice.entity.Order;
import org.example.orderservice.exception.BadRequestException;
import org.example.orderservice.exception.InternalServerErrorException;
import org.example.orderservice.exception.ResourceNotFoundException;
import org.example.orderservice.repository.OrderRepository;
import org.example.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final RestClient restClient;

    @Value("${services.product.url:http://localhost:8082/api/v1/products}")
    private String productServiceUrl;

    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        if (requestDTO.getQuantity() == null || requestDTO.getQuantity() <= 0) {
            throw new BadRequestException(OrderConstant.INVALID_QUANTITY);
        }

        // Lấy giá từ Product Service (hoặc giả lập nếu Product Service chưa khởi chạy)
        Double unitPrice = 100.0;
        try {
            ProductClientResponseDTO product = restClient.get()
                    .uri(productServiceUrl + "/" + requestDTO.getProductId())
                    .retrieve()
                    .body(ProductClientResponseDTO.class);

            if (product != null && product.getPrice() != null) {
                unitPrice = product.getPrice();
                log.info("Lấy giá thành công từ Product Service: {} cho productId={}", unitPrice, requestDTO.getProductId());
            }
        } catch (Exception e) {
            log.warn("Không kết nối được Product Service ({}), sử dụng đơn giá giả lập mặc định: {}", productServiceUrl, unitPrice);
        }

        Double totalAmount = unitPrice * requestDTO.getQuantity();

        try {
            Order order = Order.builder()
                    .customerId(requestDTO.getCustomerId())
                    .productId(requestDTO.getProductId())
                    .orderDate(LocalDateTime.now())
                    .totalAmount(totalAmount)
                    .build();

            Order saved = orderRepository.save(order);
            return mapToResponse(saved);
        } catch (Exception e) {
            log.error("Lỗi khi lưu đơn hàng vào DB: {}", e.getMessage(), e);
            throw new InternalServerErrorException(OrderConstant.DB_SAVE_ERROR + ": " + e.getMessage());
        }
    }

    @Override
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(OrderConstant.ORDER_NOT_FOUND, id)));
        return mapToResponse(order);
    }

    @Override
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrderResponseDTO mapToResponse(Order order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .productId(order.getProductId())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .build();
    }
}
