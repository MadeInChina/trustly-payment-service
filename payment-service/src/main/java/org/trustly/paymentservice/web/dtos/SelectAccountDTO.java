package org.trustly.paymentservice.web.dtos;

import lombok.Value;

@Value
public class SelectAccountDTO {
  String firstName;
  String lastName;
  String ssn;
  String email;
  String memberId;
  String requestId;
}
