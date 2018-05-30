package org.trustly.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.trustly.paymentservice.common.UUIDGenerator;
import org.trustly.paymentservice.common.UUIDGeneratorImpl;

@SpringBootApplication
public class PaymentServiceApplication {

  @Bean
  UUIDGenerator uuidGenerator() {
    return new UUIDGeneratorImpl();
  }

  public static void main(String[] args) {
    SpringApplication.run(PaymentServiceApplication.class, args);
  }
}
