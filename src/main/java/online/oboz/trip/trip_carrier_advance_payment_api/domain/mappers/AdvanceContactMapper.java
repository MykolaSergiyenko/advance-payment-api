package online.oboz.trip.trip_carrier_advance_payment_api.domain.mappers;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierContactDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierPage;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AdvanceContactMapper {

    AdvanceContactMapper contactMapper = Mappers.getMapper(AdvanceContactMapper.class);

    @Mapping(source = "uuid", target = "uuid")
    @Mapping(source = "contractorId", target = "contractorId")
    @Mapping(source = "phoneNumber", target = "info.phone")
    @Mapping(source = "email", target = "info.email")
    @Mapping(source = "fullName", target = "info.fullName")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AdvanceContactsBook toContactBook(CarrierContactDTO contactDTO);


    @InheritInverseConfiguration
    CarrierContactDTO toContactDTO(AdvanceContactsBook contact);



}
