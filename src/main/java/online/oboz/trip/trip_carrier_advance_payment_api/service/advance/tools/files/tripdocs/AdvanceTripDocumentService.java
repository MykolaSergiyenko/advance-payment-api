package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files.tripdocs;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.attachments.TripAttachment;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripDocumentsRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.ErrorUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с вложениями трипа
 *
 * @author s‡oodent
 */
@Service
public class AdvanceTripDocumentService implements TripDocumentsService {
    private static final Logger log = LoggerFactory.getLogger(AdvanceTripDocumentService.class);

    private final TripDocumentsRepository tripDocumentsRepository;

    public AdvanceTripDocumentService(TripDocumentsRepository tripDocumentsRepository) {
        this.tripDocumentsRepository = tripDocumentsRepository;
    }

    public Boolean isAllDocumentsLoaded(Advance advance) {
        return advance.getUuidContractApplicationFile() != null &&
            advance.getUuidAdvanceApplicationFile() != null;
    }

    public Boolean isAllTripDocumentsLoaded(Long tripId) {
        List<TripAttachment> attachments = null;
        try {
            attachments = getTripAttachments(tripId);
        } catch (BusinessLogicException e) {
            log.error("Trip-documents not found for tripId = {}", tripId);
        }
        return (null != attachments) && (getRequestUuidOrTripRequestUuid(attachments) != null);
    }

    public List<TripAttachment> getTripAttachments(Long tripId) {
        List<TripAttachment> attachments = tripDocumentsRepository.findByTripId(tripId);
        if (attachments.size() == 0) throw tripAttachmentsError("Attachments not found for tripId = " + tripId);
        else return attachments;
    }

    public UUID saveAssignmentAdvanceRequestUuid(Long tripId, UUID fileUuid) {
        log.info("Try to save assignment advance-request to documents of Trip: {}.", tripId);
        if (!(StringUtils.isEmptyLongs(tripId) || fileUuid == null)) {
            TripAttachment attachment = createAssignmentAttachment(tripId, fileUuid);
            log.info("Assignment advance-request saved for trip: {}.", tripId);
            return attachment.getFileId();
        } else {
            throw tripAttachmentsError("Empty ids: tripId = " + tripId + ", fileUuid = " + fileUuid);
        }
    }

    public UUID getRequestUuidOrTripRequestUuid(List<TripAttachment> attachments) {
        if (attachments == null) return null;
        UUID requestUuid = getRequestUuid(attachments);
        if (requestUuid == null) requestUuid = getTripRequestUuid(attachments);
        log.info("Request Or Trip-request file-uuid is: {}.", requestUuid);
        return requestUuid;
    }

    public UUID getRequestUuid(List<TripAttachment> attachments) {
        if (attachments == null) return null;
        return getFileUuid(attachments, "request");
    }

    public UUID getTripRequestUuid(List<TripAttachment> attachments) {
        if (attachments == null) return null;
        return getFileUuid(attachments, "trip_request");
    }

    public UUID getAssignmentRequestUuid(List<TripAttachment> attachments) {
        if (attachments == null) return null;
        return getFileUuid(attachments, "assignment_advance_request");
    }

    //***

    private UUID getFileUuid(List<TripAttachment> attachments, String fileType) {
        TripAttachment file = attachments.stream().
            filter(a -> a.getDocumentTypeCode().equals(fileType)).findFirst().orElse(null);
        if (file == null) {
            log.info("File type not found: {}", fileType);
            return null;
        } else {
            log.info("File type found: {}", fileType);
            return file.getFileId();
        }
    }

    private TripAttachment createAssignmentAttachment(Long tripId, UUID fileUuid) {
        TripAttachment assignmentAdvanceRequest = new TripAttachment(tripId, fileUuid);
        return saveTripAttachment(assignmentAdvanceRequest);
    }

    private TripAttachment saveTripAttachment(TripAttachment attachment) {
        tripDocumentsRepository.save(attachment);
        return attachment;
    }

    private BusinessLogicException tripAttachmentsError(String message) {
        return ErrorUtils.getInternalError(message);
    }


}

