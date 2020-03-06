package online.oboz.trip.trip_carrier_advance_payment_api.service;

import org.springframework.stereotype.Service;

@Service
public class AutoAdvancedService  {
//   сделать крон с выборкой трипов с  контракторами  с признаком Автоматическая выдача аванса без записис из orders.trip_request_advance_payment
//    и Перевозчик не находится в черном списке
//    затем создать записи в таблицу  orders.trip_request_advance_payment

//   сделать крон  для установки "Автоматическая выдача аванса" поставщику при суммарном количестве заказов более Х шт из консул.

//   сделать крон  для сброса поля page_carrier_url_expired (orders.trip_request_advance_payment) в значение false
//      TODO:   need add cron + sms email notity service

}
