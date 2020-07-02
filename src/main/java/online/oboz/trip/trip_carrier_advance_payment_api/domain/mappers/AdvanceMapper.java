package online.oboz.trip.trip_carrier_advance_payment_api.domain.mappers;
import com.fasterxml.jackson.annotation.JsonProperty;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContact;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvanceCommentDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvanceDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvanceDesktopDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierPage;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


@Mapper(componentModel = "spring", imports = {UUID.class, OffsetDateTime.class})
public interface AdvanceMapper {
    AdvanceMapper INSTANCE = Mappers.getMapper(AdvanceMapper.class);


    @Mapping(source = "id", target = "advanceTripFields.tripId")
    @Mapping(source = "tripCostInfo.ndsCost", target = "costInfo.ndsCost")
    @Mapping(source = "tripFields.num", target = "advanceTripFields.num")
    @Mapping(source = "tripCostInfo.cost", target = "costInfo.cost")
    @Mapping(source = "tripCostInfo.vatCode", target = "costInfo.vatCode")
    @Mapping(source = "tripFields.orderId", target = "advanceTripFields.orderId")
    @Mapping(source = "tripFields.driverId", target = "advanceTripFields.driverId")
    @Mapping(source = "contractorId", target = "contractorId")
    @Mapping(source = "tripFields.paymentContractorId", target = "advanceTripFields.paymentContractorId")
    @Mapping(source = "tripFields.tripTypeCode", target = "advanceTripFields.tripTypeCode")
    @Mapping(source = "tripFields.tripStatusCode", target = "advanceTripFields.tripStatusCode")
    Advance toAdvance(Trip trip);



    @Mapping(target = "cancelled", defaultValue = "false")
    @Mapping(source = "trip.id", target = "advanceTripFields.tripId")
    @Mapping(source = "trip.tripFields.num", target = "advanceTripFields.num")
    @Mapping(source = "trip.tripCostInfo.cost", target = "costInfo.cost")
    @Mapping(source = "trip.tripCostInfo.vatCode", target = "costInfo.vatCode")
    @Mapping(source = "trip.tripFields.orderId", target = "advanceTripFields.orderId")
    @Mapping(source = "trip.tripFields.driverId", target = "advanceTripFields.driverId")
    @Mapping(source = "trip.tripFields.paymentContractorId", target = "advanceTripFields.paymentContractorId")
    @Mapping(source = "trip.tripFields.tripTypeCode", target = "advanceTripFields.tripTypeCode")
    @Mapping(source = "trip.tripFields.tripStatusCode", target = "advanceTripFields.tripStatusCode")
//    @Mapping(source = "", target = "tripAdvanceInfo.advancePaymentSum")
//    @Mapping(target = "tripAdvanceInfo.registrationFee")
    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "contact.contractorId", target = "contractorId")
    @Mapping(source = "contact", target = "contact")
    @Mapping(target = "uuid", expression = "java(UUID.randomUUID())")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(OffsetDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(OffsetDateTime.now())")
    Advance toAdvance(Trip trip, Person author, AdvanceContact contact);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "advanceComment", target = "comment")
    Advance setComment(AdvanceCommentDTO comment);



    @Mapping(source = "id", target = "id")
    @Mapping(source = "advanceTripFields.num", target = "tripNum")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "createdAt", target = "pushButtonAt")
    @Mapping(source = "contact.info.fullName", target = "contactFio")
    @Mapping(source = "contact.info.email", target = "contactEmail")
    @Mapping(source = "contact.info.phone", target = "contactPhone")
    @Mapping(source = "auto", target = "isAutomationRequest")
    @Mapping(source = "costInfo.ndsCost", target = "tripCostWithVat")
    @Mapping(source = "tripAdvanceInfo.advancePaymentSum", target = "advancePaymentSum")
    @Mapping(source = "tripAdvanceInfo.registrationFee", target = "registrationFee")
    @Mapping(source = "loadingComplete", target = "loadingComplete")
    @Mapping(target = "isContractApplicationLoaded",
        expression = "java(!(advance.getUuidContractApplicationFile().isEmpty()))")
    @Mapping(target = "isAdvanceApplicationLoaded",
        expression = "java(!(advance.getUuidAdvanceApplicationFile().isEmpty()))")
    @Mapping(source = "1CSendAllowed", target = "is1CSendAllowed")
    @Mapping(source = "unfSend", target = "isUnfSend")
    @Mapping(target = "isPushedUnfButton", expression = "java(!advance.isUnfSend())")
    @Mapping(source = "paid", target = "isPaid")
    @Mapping(source = "paidAt", target = "paidAt")
    @Mapping(source = "comment", target = "comment")
    @Mapping(source = "cancelled", target = "isCancelled")
    @Mapping(source = "cancelledComment", target = "cancelledComment")
    AdvanceDTO toAdvanceDTO(Advance advance);

    @IterableMapping(elementTargetType = AdvanceDTO.class)
    List<AdvanceDTO> toAdvancesDTO(List<Advance> advances);

//    @Mapping(source = "advances", target = "advances")
//    AdvanceDesktopDTO toPageDTO(List<Advance> advances);



    @Mapping(target = "pageCarrierUrlIsAccess", expression = "java(!advance.isUnfSend())")
    @Mapping(source = "advanceTripFields.num", target = "tripNum")
    @Mapping(source = "cancelled", target = "isCancelled")
    @Mapping(source = "costInfo.ndsCost", target = "tripCostWithVat")
    @Mapping(source = "tripAdvanceInfo.advancePaymentSum", target = "advancePaymentSum")
    @Mapping(source = "tripAdvanceInfo.registrationFee", target = "registrationFee")
    @Mapping(source = "loadingComplete", target = "loadingComplete")
    @Mapping(source = "loadingLocation.locationTz", target = "loadingTz")
    @Mapping(source = "unloadingLocation.locationTz", target = "unloadingTz")
    @Mapping(source = "loadingLocation.address", target = "loadingAddress")
    @Mapping(source = "unloadingLocation.address", target = "unloadingAddress")
    CarrierPage toCarrierPage(Advance advance);
}
