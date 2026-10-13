package org.example.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.productservice.constant.ProductConstant;
import org.example.productservice.dto.request.ProductRequestDTO;
import org.example.productservice.dto.response.ProductResponseDTO;
import org.example.productservice.exception.GlobalExceptionHandler;
import org.example.productservice.exception.ResourceNotFoundException;
import org.example.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ProductController.class, GlobalExceptionHandler.class})
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private ProductService productService;

    @Test
    void testCreateProduct_Success() throws Exception {
        ProductRequestDTO request = ProductRequestDTO.builder()
                .name("Laptop Dell XPS")
                .price(1500.0)
                .stockQuantity(10)
                .build();

        ProductResponseDTO response = ProductResponseDTO.builder()
                .id(1L)
                .name("Laptop Dell XPS")
                .price(1500.0)
                .stockQuantity(10)
                .build();

        when(productService.createProduct(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Laptop Dell XPS"))
                .andExpect(jsonPath("$.price").value(1500.0))
                .andExpect(jsonPath("$.stockQuantity").value(10));
    }

    @Test
    void testCreateProduct_ValidationFails_WhenPriceIsNegative() throws Exception {
        // Yêu cầu: Nếu gửi một yêu cầu tạo sản phẩm với price = -500, hệ thống không được lưu vào DB mà phải trả về lỗi 400 Bad Request với định dạng ApiResponseError
        ProductRequestDTO invalidRequest = ProductRequestDTO.builder()
                .name("Bàn phím cơ")
                .price(-500.0)
                .stockQuantity(5)
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testGetProductById_NotFound() throws Exception {
        when(productService.getProductById(99L))
                .thenThrow(new ResourceNotFoundException(String.format(ProductConstant.PRODUCT_NOT_FOUND, 99L)));

        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Sản phẩm với ID 99 không tồn tại!"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testGetAllProducts() throws Exception {
        ProductResponseDTO p1 = ProductResponseDTO.builder()
                .id(1L)
                .name("Chuột không dây")
                .price(25.0)
                .stockQuantity(50)
                .build();

        when(productService.getAllProducts()).thenReturn(List.of(p1));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Chuột không dây"));
    }
}
