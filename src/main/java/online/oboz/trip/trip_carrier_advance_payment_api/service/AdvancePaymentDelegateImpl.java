package online.oboz.trip.trip_carrier_advance_payment_api.service;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.AdvancePaymentCost;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripRequestAdvancePayment;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvancePaymentCostRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripRequestAdvancePaymentRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.controller.AdvancePaymentApiDelegate;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AdvancePaymentDelegateImpl implements AdvancePaymentApiDelegate {
    private final AdvancePaymentCostRepository advancePaymentCostRepository;
    private final TripRequestAdvancePaymentRepository tripRequestAdvancePaymentRepository;
    private final TripRepository tripRepository;

    @Autowired
    public AdvancePaymentDelegateImpl(AdvancePaymentCostRepository advancePaymentCostRepository,
                                      TripRequestAdvancePaymentRepository tripRequestAdvancePaymentRepository,
                                      TripRepository tripRepository) {
        this.advancePaymentCostRepository = advancePaymentCostRepository;
        this.tripRequestAdvancePaymentRepository = tripRequestAdvancePaymentRepository;
        this.tripRepository = tripRepository;
    }

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

    // 1 day c filter c pagable   select from orders.trip_request_advance_payment   При попадании заявки на авансирование на рабочий стол должны заполняться поля «Сумма аванса с НДС» и «Сбор за оформление документов» автоматически.
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
    public ResponseEntity<ResponseOrders> getAdvancePayment(Filter filter) {
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
//2 day
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
    public ResponseEntity<Void> requestGiveAdvancePayment(Long tripId,String comment) {
        BigDecimal tripCost = tripRepository.getTripCost(tripId);
        AdvancePaymentCost advancePaymentCost = advancePaymentCostRepository.searchAdvancePaymentCost(tripCost);
        boolean isUnfSend = sendedUnf();
        Optional<Trip> trip = tripRepository.findById(tripId);
        final Trip tripDto = trip.get();
        TripRequestAdvancePayment tripRequestAdvancePayment = tripRequestAdvancePaymentRepository
            .findRequestAdvancePayment(tripId,
                tripDto.getDriverId(),
                tripDto.getContractorId());
        if (tripRequestAdvancePayment == null) {
            tripRequestAdvancePayment = new TripRequestAdvancePayment();
        tripRequestAdvancePayment.setIsUnfSend(isUnfSend);
            tripRequestAdvancePayment.setTripCost(tripCost);
            tripRequestAdvancePayment.setAdvancePaymentSum(advancePaymentCost.getAdvancePaymentSum());
            tripRequestAdvancePayment.setRegistrationFee(advancePaymentCost.getRegistrationFee());
            tripRequestAdvancePayment.setCancelAdvance(false);
            tripRequestAdvancePayment.setComment(comment);
            tripRequestAdvancePayment.setContractorId(tripDto.getContractorId());
            tripRequestAdvancePayment.setDriverId(tripDto.getDriverId());
            tripRequestAdvancePayment.setPageCarrierUrlIsAccess(true);
            tripRequestAdvancePayment.setCreatedAt(OffsetDateTime.now());
            tripRequestAdvancePayment.setTripId(tripId);
//            tripRequestAdvancePayment.setTripType();
//            tripRequestAdvancePayment.setVat();

            tripRequestAdvancePaymentRepository.save(tripRequestAdvancePayment);
        }

        return null;
    }

    @Override
    public ResponseEntity<Void> uploadFile(MultipartFile filename) {
        return null;
    }

    Boolean sendedUnf() {
        return true;
    }
}
