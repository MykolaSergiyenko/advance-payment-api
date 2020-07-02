package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.advance;


import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;


@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AuthorsAdvance extends UnfAdvanceFields {
    private static final Logger log = LoggerFactory.getLogger(AuthorsAdvance.class);

    @Column(name = "author_id", updatable = false, insertable = false)
    private Long authorId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Person author;







    public AuthorsAdvance() {
    }




    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Person getAuthor() {
        return author;
    }

    public void setAuthor(Person author) {
        this.author = author;
    }

    public void setAdvanceAuthor(Person author) {
        if (author != null) {
            setAuthorId(author.getId());
            setAuthor(author);
        } else {
            log.error("Advance AUTHOR is null.");
        }
    }


//    public String getComment() {
//        return comment;
//    }
//
//    public void setComment(String comment) {
//        this.comment = comment;
//    }

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
        return "AuthorsAdvance{" +
//            "comment='" + comment + '\'' +
            ", authorId=" + authorId +
            ", author=" + author +
//            ", tripFields=" + advanceTripFields +
//            ", tripCostInfo=" + tripCostInfo +
//            ", tripAdvanceInfo=" + tripAdvanceInfo +
//            ", locations=" + locations +
            '}';
    }
}
