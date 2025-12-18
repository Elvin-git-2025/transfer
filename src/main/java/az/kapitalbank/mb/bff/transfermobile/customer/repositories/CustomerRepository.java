package az.kapitalbank.mb.bff.transfermobile.customer.repositories;


import az.kapitalbank.mb.bff.transfermobile.customer.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

}
