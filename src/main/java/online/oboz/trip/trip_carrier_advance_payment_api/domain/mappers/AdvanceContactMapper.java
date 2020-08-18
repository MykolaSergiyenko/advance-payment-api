package online.oboz.trip.trip_carrier_advance_payment_api.domain.mappers;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contacts.FullNamePersonInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierContactDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Маппер аванса - маппим только "страницы"
 *
 * @author
 */
@Mapper(componentModel = "spring", imports = {UUID.class, OffsetDateTime.class})
public interface AdvanceContactMapper {

    AdvanceContactMapper contactMapper = Mappers.getMapper(AdvanceContactMapper.class);



    @Mapping(source = "uuid", target = "uuid")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "contractorId", target = "contractorId")
    @Mapping(source = "info.phone", target = "phoneNumber")
    @Mapping(source = "info.email", target = "email")
    @Mapping(source = "info.fullName", target = "fullName")
    CarrierContactDTO toContactDTO(AdvanceContactsBook contact);

    @InheritInverseConfiguration
    AdvanceContactsBook toContactBook(CarrierContactDTO contactDTO);


    @Mapping(source = "phoneNumber", target = "phone")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "fullName", target = "fullName")
    FullNamePersonInfo toPersonInfo(CarrierContactDTO contactDTO);

}
