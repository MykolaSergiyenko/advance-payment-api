package online.oboz.trip.trip_carrier_advance_payment_api.domain.mappers;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierContactDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class, OffsetDateTime.class})
public interface AdvanceContactMapper {

    AdvanceContactMapper contactMapper = Mappers.getMapper(AdvanceContactMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", expression = "java(UUID.randomUUID())")
    @Mapping(target = "createdAt", expression = "java(OffsetDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(OffsetDateTime.now())")
    @Mapping(source = "contractorId", target = "contractorId")
    @Mapping(source = "phoneNumber", target = "info.phone")
    @Mapping(source = "email", target = "info.email")
    @Mapping(source = "fullName", target = "info.fullName")
    AdvanceContactsBook toContactBook(CarrierContactDTO contactDTO);


    @InheritInverseConfiguration
    CarrierContactDTO toContactDTO(AdvanceContactsBook contact);


}
