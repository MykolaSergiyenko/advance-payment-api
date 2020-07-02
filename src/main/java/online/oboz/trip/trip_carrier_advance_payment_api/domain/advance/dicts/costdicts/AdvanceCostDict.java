package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.costdicts;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.time.OffsetDateTime;


// Join this dict to trip.Cost (...) somehow via beetween-join?

@Entity
@Table(name = "advance_payment_cost", schema = "dictionary")
public class AdvanceCostDict {

    final static Logger log = LoggerFactory.getLogger(AdvanceCostDict.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private Double minValue;
    private Double maxValue;
    private Double advancePaymentSum;
    private Double registrationFee;
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public AdvanceCostDict() {
    }



    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return "Advance Cost Dictionary {" +
            "id=" + id +
            ", minValue=" + minValue +
            ", maxValue=" + maxValue +
            ", advancePaymentSum=" + advancePaymentSum +
            ", registrationFee=" + registrationFee +
            ", createdAt=" + createdAt +
            '}';
    }
}
