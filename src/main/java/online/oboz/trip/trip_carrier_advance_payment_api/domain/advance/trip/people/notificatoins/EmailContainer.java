package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.mail.SimpleMailMessage;

public class EmailContainer {

    public SimpleMailMessage getMessage() {
        return message;
    }

    /**
     * Simple spring-mail message
     */
    private final SimpleMailMessage message;

    public EmailContainer(String from,
                          String to,
                          String subject,
                          String text) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        this.message = message;
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
        return "EmailContainer {message= " + message.toString() + "}";
    }

}
