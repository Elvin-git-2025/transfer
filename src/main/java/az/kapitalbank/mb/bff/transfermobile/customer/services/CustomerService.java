package az.kapitalbank.mb.bff.transfermobile.customer.services;


import az.kapitalbank.mb.bff.transfermobile.customer.dtos.requests.CreateCustomerRequest;
import az.kapitalbank.mb.bff.transfermobile.customer.dtos.responses.CustomerResponse;
import az.kapitalbank.mb.bff.transfermobile.customer.entities.Customer;
import az.kapitalbank.mb.bff.transfermobile.customer.exceptions.CustomerNotFoundException;
import az.kapitalbank.mb.bff.transfermobile.customer.mappers.CustomerMapper;
import az.kapitalbank.mb.bff.transfermobile.customer.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        Customer customer = customerMapper.toEntity(request);
        customer.setCreatedAt(LocalDateTime.now());
        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toResponse(savedCustomer);
    }

    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return customerMapper.toResponse(customer);
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerMapper.toResponseList(customerRepository.findAll());
    }


    public Customer update(Long id, Customer customer) {
        Customer updatedCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        updatedCustomer.setFirstName(customer.getFirstName());
        updatedCustomer.setLastName(customer.getLastName());
        updatedCustomer.setPin(customer.getPin());
        updatedCustomer.setDateOfBirth(customer.getDateOfBirth());

        return customerRepository.save(updatedCustomer);
    }


    public void delete(Long id) {
        customerRepository.delete(customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException(id)));
    }
}
