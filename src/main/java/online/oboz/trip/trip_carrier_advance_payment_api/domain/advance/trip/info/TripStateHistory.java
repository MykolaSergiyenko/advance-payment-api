package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.info;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.entities.BaseUpdateEntity;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * История смены статусов трипов
 *
 * @author Ⓐbo3
 */
@Entity
@Table(schema = "orders", name = "trip_transition_histories")
public class TripStateHistory extends BaseUpdateEntity {
    final static Logger log = LoggerFactory.getLogger(TripInfo.class);

    /**
     * Trip's id
     */
    @Column(name = "trip_id", updatable = false, insertable = false)
    private Long tripId;

    /**
     * Source state
     */
    @Column(name = "from_state_code")
    private String fromCode;

    /**
     * Target state
     */
    @Column(name = "to_state_code")
    private String toCode;

    /**
     * Changed author's id
     */
    @Column(name = "author_id")
    private Long authorId;

    /**
     * Comment on state changes
     */
    @Column(name = "comment")
    private String comment;


    public TripStateHistory() {
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public String getFromCode() {
        return fromCode;
    }

    public void setFromCode(String fromCode) {
        this.fromCode = fromCode;
    }

    public String getToCode() {
        return toCode;
    }

    public void setToCode(String toCode) {
        this.toCode = toCode;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
        return "TripStateHistory{" +
            "tripId=" + tripId +
            ", fromCode='" + fromCode + '\'' +
            ", toCode='" + toCode + '\'' +
            ", authorId=" + authorId +
            ", comment='" + comment + '\'' +
            '}';
    }
}
