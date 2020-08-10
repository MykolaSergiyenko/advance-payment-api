# trip-carrier-advance-payment-api
Сервис Аванс

![](advance.png)
## Зочем
Выполняет функции по выдаче аванса - денег Перевозчику (контрагенту) заранее за назначенный Трип (поездку).

Создает сущность "Аванс" и работает с ней - elp.orders.trip_request_advance_payment - связана с поездкой, заказом, контрагентом, пользователями, контактами авансирования, справочниками и т.д.

Доступ к эндпоинтам сервиса "Авансирование" прооисходит на фронте:
- на "Рабочем столе авансирования" - грид сущностей типа "аванс" с кнопками и т.д.
- на странице "Перевозчика" - страница предложения аванса контрагенту.
- в карточке "Перевозчика" - редактирование "Контактов авансирования"
- в карточке "Поездки" - статус выданного аванса\ возможность выдачи аванса по трипу.

Также существует микросервис "авто-авансирование" (AutoAdvanceService), который по расписанию автоматически выполняет обновление контрагентов и выдачу им "авансов" в будущем в автоматическом режиме. В ручном режиме "аванс" создается кнопкой "Выдать аванс" в карточке "Поездки".

![](schema.png)

## Модель данных
Основные сущности базы данных и их связи показаны на схеме:
![](model.jpg)

## Требования к окружению
Работает вместе с front, advance-front. 

Эндпоинты разделены по старницам - cм. выше.


## Запуск
SpringBoot - MainApplication

## Сборка docker image

## Запуск тестов
Есть только тесты для url-сокращатора - UrlShortenerServiceTest.


## Примечания Геннадия
Пример получения файлов с report server:
curl -o f.pdf "https://reports.oboz.com/reportserver/reportserver/httpauthexport?key=avance_request&tripAdvancePerson=bertathar&apikey=nzybc16h&p_trip_num=157966-1&p_avance_sum=300&p_avance_comission=150&format=PDF"

curl -o ff.pdf "https://reports.oboz.com/reportserver/reportserver/httpauthexport?key=unified_contract_request&tripAdvancePerson=bertathar&apikey=nzybc16h&p_trip_num=157966-1&format=PDF"

sudo keytool -importcert -keystore /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/cacerts -storepass changeit -file /home/gmakarov/Downloads/cert.oboz.crt -alias "oboz.com"
