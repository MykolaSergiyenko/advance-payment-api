package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.email;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.mail.SimpleMailMessage;

public class EmailContainer  {

    public SimpleMailMessage getMessage() {
        return message;
    }

    private final SimpleMailMessage message;

    public EmailContainer(String from,
                          String to,
                          String subject,
                          String text) {

        //from Spring
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from); //app.config
        message.setTo(to); //adcvance contact
        message.setSubject(subject); //app.config, advance
        message.setText(text); //app.config, advance

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
        return "EmailContainer{" +
            "message='" + message.toString() + '\'' +
            '}';
    }

}
