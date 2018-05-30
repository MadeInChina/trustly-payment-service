package org.trustly.paymentservice.query.trustlyOrder.enteties;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class TrustlyAccount {

    @Id
    public String trustlyAccountId;

}
