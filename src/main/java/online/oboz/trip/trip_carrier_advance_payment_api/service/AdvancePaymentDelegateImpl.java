package online.oboz.trip.trip_carrier_advance_payment_api.service;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.controller.AdvancePaymentApiDelegate;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class AdvancePaymentDelegateImpl implements AdvancePaymentApiDelegate {
    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    @Override
    public ResponseEntity<Void> addExcludedContractor(Long id) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteExcludedContractor(Long id) {
        return null;
    }

    @Override
    public ResponseEntity<Resource> downloadFile() {
        return null;
    }

    @Override
    public ResponseEntity<List<ContractorDTO>> getCarriers() {
        return null;
    }

    @Override
    public ResponseEntity<List<ContractorDTO>> getClients() {
        return null;
    }

    @Override
    public ResponseEntity<List<DocumentTypeDTO>> getDocumentTypes() {
        return null;
    }

    @Override
    public ResponseEntity<ResponseOrders> getOrderList(Filter filter) {
        return null;
    }

    @Override
    public ResponseEntity<OrderDTO> getOrderWithInfoById(Integer orderId) {
        return null;
    }

    @Override
    public ResponseEntity<List<TripStatusDTO>> getTripStatusesDict() {
        return null;
    }

    @Override
    public ResponseEntity<List<TripTypeDTO>> getTripTypesDict() {
        return null;
    }

    @Override
    public ResponseEntity<Void> giveAdvancePayment(Long id, Boolean isSuccess) {
        return null;
    }

    @Override
    public ResponseEntity<IsAdvancedRequestResponse> isAdvanced() {
        return null;
    }

    @Override
    public ResponseEntity<Void> requestGiveAdvancePayment(Long id) {
        return null;
    }

    @Override
    public ResponseEntity<Void> uploadFile(MultipartFile filename) {
        return null;
    }
}
