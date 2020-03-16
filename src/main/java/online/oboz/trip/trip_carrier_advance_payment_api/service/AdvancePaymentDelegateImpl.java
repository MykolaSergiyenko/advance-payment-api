package online.oboz.trip.trip_carrier_advance_payment_api.service;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.*;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.*;
import online.oboz.trip.trip_carrier_advance_payment_api.util.SecurityUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.controller.AdvancePaymentApiDelegate;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdvancePaymentDelegateImpl implements AdvancePaymentApiDelegate {
    private final AdvancePaymentCostRepository advancePaymentCostRepository;
    private final TripRequestAdvancePaymentRepository tripRequestAdvancePaymentRepository;
    private final ContractorAdvancePaymentContactRepository contractorAdvancePaymentContactRepository;
    private final TripRepository tripRepository;
    private final ContractorRepository contractorRepository;

    @Autowired
    public AdvancePaymentDelegateImpl(AdvancePaymentCostRepository advancePaymentCostRepository,
                                      TripRequestAdvancePaymentRepository tripRequestAdvancePaymentRepository,
                                      ContractorAdvancePaymentContactRepository contractorAdvancePaymentContactRepository,
                                      TripRepository tripRepository,
                                      ContractorRepository contractorRepository) {
        this.advancePaymentCostRepository = advancePaymentCostRepository;
        this.tripRequestAdvancePaymentRepository = tripRequestAdvancePaymentRepository;
        this.contractorAdvancePaymentContactRepository = contractorAdvancePaymentContactRepository;
        this.tripRepository = tripRepository;
        this.contractorRepository = contractorRepository;
    }

    @Override
    public ResponseEntity<Void> uploadRequestAvance(MultipartFile filename) {
        return null;
    }

    //   c filter c pagable   select from orders.trip_request_advance_payment
    // При попадании заявки на авансирование на рабочий стол должны заполняться
    // поля «Сумма аванса с НДС» и «Сбор за оформление документов» автоматически.
//    • Номер заказа поставщика – номер заказа поставщика из ОБОЗа
//    • Дата заявки - дата попадания данных в рабочий стол по авансированию (возможно присваивание «выдан аванс» в БД)
//    • Исполнитель - Наименование перевозчика
//    • Контактные данные исполнителя – Имя и телефон из карточки контрагента из «контактов для авансирования» (раздел 7)
//    • От кого везем - Юр. лицо для взаиморасчетов
//    • Время нажатия кнопки в ОБОЗ "Выдать аванс" – время нажатия кнопки в ОБОЗе или перевозчиком
//    • Стоимость перевозки с НДС
//    • Сумма аванса с НДС
//    • Сбор за оформление документов
//    • Аннулировать аванс – кнопка, при нажатии которой статус строки становится «аннулирован»
//    • Комментарий к аннулированию аванса – текстовое поле без ограничения по знакам
//    • Загрузился водитель – признак загрузки водителя (поле со значением да/нет)
//    • Загрузили документы – от поставщика поступил скан документа «Заявка на аванс»
//    • В 1С – заявка отправлена в УНФ
//    • Загружен в бухгалтерию -
//    • Оплачен – признак оплаты из УНФ
//    • Дата оплаты – дата оплаты из УНФ
//    • Комментарий – текстовое поле без ограничения по знакам
//    • Отозванная заявка – заявка, которую отозвали по ряду причин (смотреть раздел 9)
    @Override
    public ResponseEntity<ResponseAdvancePayment> searchAdvancePaymentRequest(Filter filter) {
//TODO:
        Pageable pageable = PageRequest.of(filter.getPage() - 1, filter.getPerPage());
        List<FrontAdvancePaymentResponse> responseList =
            tripRequestAdvancePaymentRepository.findTripRequestAdvancePayment(pageable).stream()
                .map(reс -> {
                    FrontAdvancePaymentResponse frontAdvancePaymentResponse = new FrontAdvancePaymentResponse();
                    ContractorAdvancePaymentContact contractorAdvancePaymentContact =
                        contractorAdvancePaymentContactRepository.findContractorAdvancePaymentContact(reс.getContractorId())
                            .orElse(new ContractorAdvancePaymentContact());
                    Contractor contractor = contractorRepository.findById(reс.getContractorId()).orElse(new Contractor());
                    String contractorPaymentName = contractorRepository.getContractors(reс.getPaymentContractorId());
                    Trip trip = tripRepository.findById(reс.getTripId()).orElse(new Trip());
                    frontAdvancePaymentResponse
                        .id(reс.getId())
                        .tripId(reс.getTripId())
                        .tripNum(trip.getNum())
                        .tripTypeCode(reс.getTripTypeCode())
                        .createdAt(trip.getCreatedAt())
                        .reqCreatedAt(reс.getCreatedAt())
                        .contractorId(reс.getContractorId())
                        .contractorName(contractor.getFullName())
                        .contactFio(contractorAdvancePaymentContact.getFullName())
                        .contactPhone(contractorAdvancePaymentContact.getPhone())
                        .contactEmail(contractorAdvancePaymentContact.getEmail())
                        .paymentContractor(contractorPaymentName)
                        .isAutomationRequest(reс.getIsAutomationRequest())
                        .tripCostWithVat(reс.getTripCost())
                        .advancePaymentSum(reс.getAdvancePaymentSum())
                        .registrationFee(reс.getRegistrationFee())
                        //проставляется вручную сотрудниками авансирования
                        .loadingComplete(reс.getLoadingComplete())
                        .urlContractApplication(reс.getUuidContractApplicationFile())
                        .urlAdvanceApplication(reс.getUuidAdvanceApplicationFile())
                        .is1CSendAllowed(reс.getIs1CSendAllowed())
                        .isUnfSend(reс.getIsUnfSend())
                        .isPaid(reс.getIsPaid())
                        .paidAt(reс.getPaidAt())
                        .comment(reс.getComment())
                        .cancelAdvance(reс.getCancelAdvance())
                        .cancelAdvanceComment(reс.getCancelAdvanceComment())
                        .authorId(reс.getAuthorId())
                        .setPageCarrierUrlIsAccess(reс.getPageCarrierUrlIsAccess());
                    return frontAdvancePaymentResponse;
                })
                .collect(Collectors.toList());
        final ResponseAdvancePayment responseAdvancePayment = new ResponseAdvancePayment();
        responseAdvancePayment.setRequestAdvancePayment(responseList);
        return new ResponseEntity<>(responseAdvancePayment, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> confirmAdvancePayment(Long requestAdvansePaymentId) {
        TripRequestAdvancePayment tripRequestAdvancePayment = getTripRequestAdvancePayment(requestAdvansePaymentId);
        Trip trip = tripRepository.findById(tripRequestAdvancePayment.getId()).orElseThrow(() -> {
                Error error = new Error();
                error.setErrorMessage("tripRequestAdvancePayment not found");
                return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
            }
        );
//        tripRequestAdvancePaymentRepository.save(tripRequestAdvancePayment.get());
        trip.setIsAdvancedPayment(true);
        tripRepository.save(trip);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @Override
    public ResponseEntity<IsAdvancedRequestResponse> isAdvanced(Long tripId) {
        Trip trip = getMotorTrip(tripId);
        TripRequestAdvancePayment tripRequestAdvancePayment = getRequestAdvancePaymentByTrip(tripId);
        IsAdvancedRequestResponse isAdvancedRequestResponse = new IsAdvancedRequestResponse();
        if (tripRequestAdvancePayment.getCancelAdvance()
            || tripRequestAdvancePayment.getIsUnfSend()
            || !trip.getDriverId().equals(tripRequestAdvancePayment.getDriverId())) {
            isAdvancedRequestResponse.setIsButtonActive(false);
        }
        isAdvancedRequestResponse.setIsAdvanssed(tripRequestAdvancePayment.getIsUnfSend());
        isAdvancedRequestResponse.setTripTypeCode(tripRequestAdvancePayment.getTripTypeCode());
        isAdvancedRequestResponse.setAuthorId(tripRequestAdvancePayment.getAuthorId());
        isAdvancedRequestResponse.setCreatedAt(tripRequestAdvancePayment.getCreatedAt());
        isAdvancedRequestResponse.setIsAutoRequested(tripRequestAdvancePayment.getIsAutomationRequest());
        return new ResponseEntity<>(isAdvancedRequestResponse, HttpStatus.OK);
    }
//    При нажатии на кнопку происходит следующий функционал:   создать таблицу запросов на авансирование orders.trip_request_advance_payment
//    сделать entity сгенерить таблицу в бд
//        создать репозиторий , заполнить ентити по списку ниже , вызвать save
//    insert
//        • На рабочем столе «Авансирования» появляется строка с данными из «Заказа поставщика», алгоритм заполнения полей авансирования следующий:
//        ◦ Номер заказа поставщика – берется номер «Заказа поставщика», например 176438-1
//        ◦ Дата заявки – дата создания «Заказа поставщика» в ОБОЗ
//        ◦ Исполнитель – название назначенного «Поставщика» в «Заказе поставщика» - contractor_id
//        ◦ Контактные данные исполнителя – ФИО и телефон из карточки «Поставщика» из полей «Контакты для авансирования»  - новая таблица - contractor_advance_contact
//        ◦ От кого везем – Юр. лицо для взаиморасчетов из ОБОЗа   -   наверное с трипа
//        ◦ Время нажатия кнопки в ОБОЗ "Выдать аванс" – время нажатия пользователем кнопки «Выдать аванс» в ОБОЗе - используем now() - UTC
//        ◦ Стоимость перевозки с НДС – Стоимость для поставщика с НДС - cost from trip with add NDS
//        ◦ Сумма аванса с НДС – рассчитывается по алгоритму - Заводим таблицу в бд ид ,min , max,аванс,сбор + запрос на получение аванс, сбор по "Стоимость для поставщика с НДС"
//        ◦ Сбор за оформление документов – рассчитывается по алгоритму (описан выше)
//        ◦ Загрузился водитель – изменяемое поле (да/нет) вручную пользователем - условие для запроса, что у трипа назначен водитель

//        ◦ Загрузили доки – в систему загружены 2 документа
//        (Заявка или Договор-заявка из «Заказа поставщика» и «Заявка на авансирование»
//        загруженная перевозчиком (на 1 этапе сделал бы возможность загружать их вручную при нажатии на данную ячейку) - проверка что к трипу есть два дока

//        ◦ В 1С – кнопка, становится активной, когда загружены документы («Заявка» или «Договор-заявка» и «Заявка на авансирование» это в другой функции
//        ◦ Загружен в бухгалтерию – заявка загружена в УНФ это в другой функции
//        ◦ Оплачен – заявка оплачена в бухгалтерии это в другой функции
//        ◦ Дата оплаты – дата оплаты в бухгалтерии это в другой функции
//        ◦ Комментарий – текстовое поле без ограничений по знакам это в другой функции
//        ◦ Аннулировать аванс – кнопка, которая проставляет признак «аннулирован», при нажатии на кнопку появляется признак, т.е. есть 2 варианта или кнопка или признак   это в другой функции
//                                                                             ◦ Комментарий к аннулированию аванса – текстовое поле без ограничений по знакам       это в другой функции
//                                                                             ◦ Отозванная заявка – признак «да/нет»  это в другой функции

//  как отправить СМС? время не известно предлагаю не отправлять перевозчикам из черного списка
//  как отправить email ? время 1день предлагаю не отправлять перевозчикам из черного списка
    //

//        • Поставщику уходит на почту и смс на телефон описание в разделе 6.
//
//Данную кнопку можно нажать 1 раз. - проверить что нет записи по уникальным полям в таблице orders.trip_request_advance_payment  возвращаем ошибку
// При нажатии на копку записывается дата и время нажатия + ФИО кто нажал.  -   записываем person_id в orders.trip_request_advance_payment.
//
// При наведении на кнопку всплывает диалоговое окно с информацией (дата, время, ФИО). это в другой функции
//Если запрос на аванс ушел автоматически – кнопка будет серая, при наведении на нее всплывает диалоговое окно с текстом «Данному поставщику отправлен запрос на аванс в автоматическом режиме + дата и время отправки запроса»  это в другой функции
//После смены перевозчика в «Заказе поставщика» кнопка становится снова активной (статус с «Назначен» поменялся на следующие: «Подтвержден перевозчиком», «Ожидание подтверждения водителем», «Отказ перевозчика»)  это в другой функции
//Исключением является назначение перевозчика и поставщика, которому уже выдали аванс, т. е. у одного заказа не могут быть несколько заявок на аванс с одним и тем же перевозчиком и водителем.
//
//Если контрагент в списке исключений, т. е. в АРМА в столбике «Выдавать аванс» стоит «нет» (раздел 8), кнопка не активна, при наведении всплывает рядом с кнопкой окно с текстом «Данный контрагент находится в списке исключений для авансирования».
//Кнопка «Выдать аванс» недоступна, пока не подгружен любой из документов: «Заявка» или «Договор-заявка» в «Заказе поставщика». Данное исключение нужно сделать с возможностью отключения в АРМА, описание в пункте 8.

    @Override
    public ResponseEntity<Void> requestGiveAdvancePayment(Long tripId, String comment) {
        Double tripCostWithNds = tripRepository.getTripCostWithVat(tripId);
        AdvancePaymentCost advancePaymentCost = advancePaymentCostRepository.searchAdvancePaymentCost(tripCostWithNds);
        boolean isUnfSend = sendedUnf();
        Trip trip = tripRepository.getMotorTrip(tripId).orElse(new Trip());

        if (advancePaymentCost == null || trip.getDriverId() == null || trip.getContractorId() == null) {
            Error error = new Error();
            error.setErrorMessage("");
            throw new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
        }
        TripRequestAdvancePayment tripRequestAdvancePayment = tripRequestAdvancePaymentRepository
            .findRequestAdvancePayment(tripId,
                trip.getDriverId(),
                trip.getContractorId());
        if (tripRequestAdvancePayment != null) {
            Error error = new Error();
            error.setErrorMessage("tripRequestAdvancePayment is present");
            throw new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
        }
        tripRequestAdvancePayment = getTripRequestAdvancePayment(tripId,
            comment,
            tripCostWithNds,
            advancePaymentCost,
            isUnfSend,
            trip);
        // отключаем доступ в страницу поставщика
        tripRequestAdvancePayment.setPageCarrierUrlIsAccess(false);
        tripRequestAdvancePaymentRepository.save(tripRequestAdvancePayment);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @Override
    public ResponseEntity<Resource> downloadAvanceRequestTemplate(String tripNum) {
        return null;
    }

    @Override
    public ResponseEntity<Void> addContactCarrier(CarrierContactDTO carrierContactDTO) {
        Optional<ContractorAdvancePaymentContact> contractorAdvancePaymentContact =
            contractorAdvancePaymentContactRepository.findContractorAdvancePaymentContact(carrierContactDTO.getContractorId());
        if (contractorAdvancePaymentContact.isPresent()) {
            Error error = new Error();
            error.setErrorMessage("ContractorAdvancePaymentContact is present");
            throw new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
        }
        final ContractorAdvancePaymentContact entity = new ContractorAdvancePaymentContact();
        entity.setFullName(carrierContactDTO.getFullName());
        entity.setContractorId(carrierContactDTO.getContractorId());
        entity.setPhone(carrierContactDTO.getPhoneNumber());
        entity.setEmail(carrierContactDTO.getEmail());
        contractorAdvancePaymentContactRepository.save(entity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Resource> downloadAvanseRequest(String tripNum) {
        return null;
    }

    @Override
    public ResponseEntity<Resource> downloadRequest(String tripNum) {
        return null;
    }

    @Override
    public ResponseEntity<Void> updateLoadingComplete(Long id, Boolean loadingComplete) {
        final TripRequestAdvancePayment entity = getTripRequestAdvancePayment(id);
        entity.setLoadingComplete(loadingComplete);
        tripRequestAdvancePaymentRepository.save(entity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateContactCarrier(CarrierContactDTO carrierContactDTO) {
        Optional<ContractorAdvancePaymentContact> contractorAdvancePaymentContact =
            contractorAdvancePaymentContactRepository.findContractorAdvancePaymentContact(carrierContactDTO.getContractorId());
        final ContractorAdvancePaymentContact entity = contractorAdvancePaymentContact.orElseThrow(() -> {
                Error error = new Error();
                error.setErrorMessage("ContractorAdvancePaymentContact not found");
                return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
            }
        );
        entity.setFullName(carrierContactDTO.getFullName());
        entity.setContractorId(carrierContactDTO.getContractorId());
        entity.setPhone(carrierContactDTO.getPhoneNumber());
        entity.setEmail(carrierContactDTO.getEmail());
        contractorAdvancePaymentContactRepository.save(entity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> cancelAdvancePayment(Long id, String cancelAdvanceComment) {
        final TripRequestAdvancePayment entity = getTripRequestAdvancePayment(id);
        entity.setCancelAdvanceComment(cancelAdvanceComment);
        tripRequestAdvancePaymentRepository.save(entity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CarrierContactDTO> getContactCarrier(Long contractorId) {
        ContractorAdvancePaymentContact contact = contractorAdvancePaymentContactRepository.findContractorAdvancePaymentContact(contractorId)
            .orElseThrow(() -> {
                    Error error = new Error();
                    error.setErrorMessage("carrierContact not found");
                    return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
                }
            );
        CarrierContactDTO carrierContactDTO = getCarrierContactDTO(contact);
        return new ResponseEntity<>(carrierContactDTO, HttpStatus.OK);
    }


    private Trip getMotorTrip(Long tripId) {
        return tripRepository.getMotorTrip(tripId).orElseThrow(() -> {
                Error error = new Error();
                error.setErrorMessage("trip not found");
                return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
            }
        );
    }

    private TripRequestAdvancePayment getRequestAdvancePaymentByTrip(Long tripId) {
        return tripRequestAdvancePaymentRepository
            .findTripRequestAdvancePayment(tripId).orElseThrow(() -> {
                    Error error = new Error();
                    error.setErrorMessage("TripRequestAdvancePayment not found");
                    return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
                }
            );
    }

    private TripRequestAdvancePayment getTripRequestAdvancePayment(Long tripId, String comment, Double tripCostWithNds, AdvancePaymentCost advancePaymentCost, boolean isUnfSend, Trip trip) {
        TripRequestAdvancePayment tripRequestAdvancePayment;
        tripRequestAdvancePayment = new TripRequestAdvancePayment();
        tripRequestAdvancePayment.setAuthorId(SecurityUtils.getAuthPersonId());
        tripRequestAdvancePayment.setTripId(advancePaymentCost.getId());
        tripRequestAdvancePayment.setIsUnfSend(isUnfSend);
        tripRequestAdvancePayment.setTripCost(tripCostWithNds);
        tripRequestAdvancePayment.setAdvancePaymentSum(advancePaymentCost.getAdvancePaymentSum());
        tripRequestAdvancePayment.setRegistrationFee(advancePaymentCost.getRegistrationFee());
        tripRequestAdvancePayment.setCancelAdvance(false);
        tripRequestAdvancePayment.setComment(comment);
        tripRequestAdvancePayment.setContractorId(trip.getContractorId());
        tripRequestAdvancePayment.setDriverId(trip.getDriverId());
        tripRequestAdvancePayment.setPageCarrierUrlIsAccess(true);
        tripRequestAdvancePayment.setCreatedAt(OffsetDateTime.now());
        tripRequestAdvancePayment.setTripId(tripId);
        tripRequestAdvancePayment.setTripTypeCode(trip.getTripTypeCode());
        tripRequestAdvancePayment.setLoadingComplete(false);
        tripRequestAdvancePayment.setPaymentContractorId(trip.getPaymentContractorId());

        //TODO: new fields
        tripRequestAdvancePayment.setIsPaid(false);
        tripRequestAdvancePayment.setPaidAt(OffsetDateTime.now());
        tripRequestAdvancePayment.setCancelAdvanceComment("");
        tripRequestAdvancePayment.setIsAutomationRequest(false);

        return tripRequestAdvancePayment;
    }

    Boolean sendedUnf() {
        return true;
    }


    private TripRequestAdvancePayment getTripRequestAdvancePayment(Long id) {
        Optional<TripRequestAdvancePayment> tripRequestAdvancePayment = tripRequestAdvancePaymentRepository
            .findTripRequestAdvancePayment(id);
        return tripRequestAdvancePayment.orElseThrow(() -> {
                Error error = new Error();
                error.setErrorMessage("TripRequestAdvancePayment not found");
                return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
            }
        );
    }

    private CarrierContactDTO getCarrierContactDTO(ContractorAdvancePaymentContact contractorAdvancePaymentContact) {
        CarrierContactDTO carrierContactDTO = new CarrierContactDTO();
        carrierContactDTO.setContractorId(contractorAdvancePaymentContact.getContractorId());
        carrierContactDTO.setEmail(contractorAdvancePaymentContact.getEmail());
        carrierContactDTO.setFullName(contractorAdvancePaymentContact.getFullName());
        carrierContactDTO.setPhoneNumber(contractorAdvancePaymentContact.getPhone());
        return carrierContactDTO;
    }
}
