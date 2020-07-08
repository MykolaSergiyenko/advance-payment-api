package online.oboz.trip.trip_carrier_advance_payment_api.domain.mappers;


import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.info.TripInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvanceCommentDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvanceDTO;
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
    AdvanceMapper advanceMapper = Mappers.getMapper(AdvanceMapper.class);


    @Mapping(source = "id", target = "advanceTripFields.tripId")
    @Mapping(source = "tripCostInfo.cost", target = "costInfo.cost")
    @Mapping(source = "tripFields.orderId", target = "advanceTripFields.orderId")
    @Mapping(source = "tripFields.driverId", target = "advanceTripFields.driverId")
    @Mapping(source = "contractorId", target = "contractorId")
    @Mapping(source = "tripFields.paymentContractorId", target = "advanceTripFields.paymentContractorId")
    @Mapping(source = "tripFields.tripTypeCode", target = "advanceTripFields.tripTypeCode")
    Advance toAdvance(Trip trip);
    // don't use. or toAdvance(Trip trip, Person author...) too.


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
    @Mapping(source = "costInfo.cost", target = "tripCostWithVat")
    @Mapping(source = "tripAdvanceInfo.advancePaymentSum", target = "advancePaymentSum")
    @Mapping(source = "tripAdvanceInfo.registrationFee", target = "registrationFee")
    @Mapping(source = "loadingComplete", target = "loadingComplete")
    @Mapping(target = "isContractApplicationLoaded",
        expression = "java(!(null == advance.getUuidContractApplicationFile()))")
    @Mapping(target = "isAdvanceApplicationLoaded",
        expression = "java(!(null == advance.getUuidAdvanceApplicationFile()))")
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


    @Mapping(source = "advance.id", target = "id")
    @Mapping(source = "advance.advanceTripFields.num", target = "tripNum")
    @Mapping(source = "advance.cancelled", target = "isCancelled")
    @Mapping(source = "advance.costInfo.cost", target = "tripCostWithVat")
    @Mapping(source = "advance.tripAdvanceInfo.advancePaymentSum", target = "advancePaymentSum")
    @Mapping(source = "advance.tripAdvanceInfo.registrationFee", target = "registrationFee")
    @Mapping(source = "advance.loadingComplete", target = "loadingComplete")
    @Mapping(source = "advance.auto", target = "isAuto")
    @Mapping(target = "isWanted", expression = "java(!(null == advance.getPushButtonAt() || " +
        "advance.getPushButtonAt().toString().isEmpty()) && advance.isAuto())")
    @Mapping(source = "tripInfo.startLocation.locationTz", target = "loadingTz")
    @Mapping(source = "tripInfo.endLocation.locationTz", target = "unloadingTz")
    @Mapping(source = "tripInfo.startLocation.address", target = "loadingAddress")
    @Mapping(source = "tripInfo.endLocation.address", target = "unloadingAddress")
    @Mapping(source = "tripInfo.startDate", target = "loadingDate")
    @Mapping(source = "tripInfo.endDate", target = "unloadingDate")
    CarrierPage toCarrierPage(Advance advance, TripInfo tripInfo);

}
