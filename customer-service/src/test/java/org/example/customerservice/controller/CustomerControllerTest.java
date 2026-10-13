package org.example.customerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.customerservice.constant.CustomerConstant;
import org.example.customerservice.dto.request.CustomerRequestDTO;
import org.example.customerservice.dto.request.LoginRequestDTO;
import org.example.customerservice.dto.response.CustomerResponseDTO;
import org.example.customerservice.exception.BadRequestException;
import org.example.customerservice.exception.GlobalExceptionHandler;
import org.example.customerservice.exception.ResourceNotFoundException;
import org.example.customerservice.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {CustomerController.class, GlobalExceptionHandler.class})
public class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private CustomerService customerService;

    @Test
    void testRegisterSuccess() throws Exception {
        CustomerRequestDTO request = CustomerRequestDTO.builder()
                .fullName("Nguyen Van A")
                .email("vana@example.com")
                .password("password123")
                .build();

        CustomerResponseDTO response = CustomerResponseDTO.builder()
                .id(1L)
                .fullName("Nguyen Van A")
                .email("vana@example.com")
                .build();

        when(customerService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/customers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fullName").value("Nguyen Van A"))
                .andExpect(jsonPath("$.email").value("vana@example.com"));
    }

    @Test
    void testGetCustomerNotFound_Returns404AndApiResponseError() throws Exception {
        when(customerService.getCustomerById(99L))
                .thenThrow(new ResourceNotFoundException(String.format(CustomerConstant.CUSTOMER_NOT_FOUND, 99L)));

        mockMvc.perform(get("/api/v1/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Khách hàng với ID 99 không tồn tại!"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testLoginFailed_Returns400AndApiResponseError() throws Exception {
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .email("wrong@example.com")
                .password("wrongpassword")
                .build();

        when(customerService.login(any()))
                .thenThrow(new BadRequestException(CustomerConstant.LOGIN_FAILED));

        mockMvc.perform(put("/api/v1/customers/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("email or password incorrect"));
    }
}
