package org.example.customerservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.customerservice.constant.CustomerConstant;
import org.example.customerservice.dto.request.CustomerRequestDTO;
import org.example.customerservice.dto.request.LoginRequestDTO;
import org.example.customerservice.dto.response.CustomerResponseDTO;
import org.example.customerservice.entity.Customer;
import org.example.customerservice.exception.BadRequestException;
import org.example.customerservice.exception.ResourceNotFoundException;
import org.example.customerservice.repository.CustomerRepository;
import org.example.customerservice.service.CustomerService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public CustomerResponseDTO register(CustomerRequestDTO requestDTO) {
        if (customerRepository.existsByEmail(requestDTO.getEmail())) {
            throw new BadRequestException(CustomerConstant.EMAIL_ALREADY_EXISTS);
        }
        Customer customer = Customer.builder()
                .fullName(requestDTO.getFullName())
                .email(requestDTO.getEmail())
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .build();
        Customer saved = customerRepository.save(customer);
        return mapToResponse(saved);
    }

    @Override
    public CustomerResponseDTO login(LoginRequestDTO loginDTO) {
        Customer customer = customerRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new BadRequestException(CustomerConstant.LOGIN_FAILED));

        if (!passwordEncoder.matches(loginDTO.getPassword(), customer.getPassword())) {
            throw new BadRequestException(CustomerConstant.LOGIN_FAILED);
        }

        return mapToResponse(customer);
    }

    @Override
    public CustomerResponseDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CustomerConstant.CUSTOMER_NOT_FOUND, id)));
        return mapToResponse(customer);
    }

    @Override
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO requestDTO) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CustomerConstant.CUSTOMER_NOT_FOUND, id)));

        if (!customer.getEmail().equals(requestDTO.getEmail()) && customerRepository.existsByEmail(requestDTO.getEmail())) {
            throw new BadRequestException(CustomerConstant.EMAIL_ALREADY_EXISTS);
        }

        customer.setFullName(requestDTO.getFullName());
        customer.setEmail(requestDTO.getEmail());
        if (requestDTO.getPassword() != null && !requestDTO.getPassword().isBlank()) {
            customer.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        }
        return mapToResponse(customerRepository.save(customer));
    }

    @Override
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CustomerConstant.CUSTOMER_NOT_FOUND, id)));
        customerRepository.delete(customer);
    }

    private CustomerResponseDTO mapToResponse(Customer customer) {
        return CustomerResponseDTO.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .build();
    }
}
