package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.advance;


import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;


/**
 * "Авторский аванс"
 * - у аванса всегда есть автор
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AuthorsAdvance extends UnfAdvanceFields {

    /**
     * Advance author id
     */
    @Column(name = "author_id", updatable = false, insertable = false)
    private Long authorId;

    /**
     * Advance author
     * (Person who push 'Give advance'-button or auto-advance-author)
     */
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
            ", authorId=" + authorId +
            ", author=" + author +
            '}';
    }
}
