package org.example.orderservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDTO {

    @NotNull(message = "customerId không được để trống")
    private Long customerId;

    @NotNull(message = "productId không được để trống")
    private Long productId;

    @NotNull(message = "quantity không được để trống")
    private Integer quantity;
}
