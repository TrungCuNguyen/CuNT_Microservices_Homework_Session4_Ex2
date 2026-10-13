package org.example.orderservice.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductClientResponseDTO {
    private Long id;
    private String name;
    private Double price;
    private Integer stockQuantity;
}
