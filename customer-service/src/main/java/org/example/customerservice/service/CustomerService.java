package org.example.customerservice.service;

import org.example.customerservice.dto.request.CustomerRequestDTO;
import org.example.customerservice.dto.request.LoginRequestDTO;
import org.example.customerservice.dto.response.CustomerResponseDTO;

import java.util.List;

public interface CustomerService {
    CustomerResponseDTO register(CustomerRequestDTO requestDTO);
    CustomerResponseDTO login(LoginRequestDTO loginDTO);
    CustomerResponseDTO getCustomerById(Long id);
    List<CustomerResponseDTO> getAllCustomers();
    CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO requestDTO);
    void deleteCustomer(Long id);
}
