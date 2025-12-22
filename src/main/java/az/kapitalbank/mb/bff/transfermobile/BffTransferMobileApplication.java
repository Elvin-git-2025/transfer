package az.kapitalbank.mb.bff.transfermobile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableFeignClients
public class BffTransferMobileApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffTransferMobileApplication.class, args);
    }
}
