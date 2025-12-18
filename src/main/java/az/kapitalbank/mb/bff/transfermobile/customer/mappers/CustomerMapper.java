package az.kapitalbank.mb.bff.transfermobile.customer.mappers;

import az.kapitalbank.mb.bff.transfermobile.customer.dtos.requests.CreateCustomerRequest;
import az.kapitalbank.mb.bff.transfermobile.customer.dtos.responses.CustomerResponse;
import az.kapitalbank.mb.bff.transfermobile.customer.entities.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
@Mapper(componentModel = "spring")
public interface CustomerMapper {

    Customer toEntity(CreateCustomerRequest request);

    @Mapping(target = "age", expression = "java(customer.getAge())")
    CustomerResponse toResponse(Customer customer);

    List<CustomerResponse> toResponseList(List<Customer> customers);
}
