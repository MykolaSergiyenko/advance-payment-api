package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.advance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.util.ErrorUtils;

public enum AdvanceEvent {

    LOAD_DOCS(10, "loaded_docs", "Документы загружены"),
    COMPLETE_LOADING(20, "complete_loading", "Загрузка завершена"),
    SEND_UNF(30, "send_unf", "Отправить аванс в УНФ"),
    PAY(40, "pay_advance", "Выплатить аванс"),
    SET_COMMENT(50, "comment_advance", "Установить комментарий"),
    CANCEL(60, "cancel_advance", "Отменить аванс");

    private long advanceEventId;
    private String eventName;
    private String eventDesc;

    AdvanceEvent(long advanceEventId, String eventName, String eventDesc) {
        this.advanceEventId = advanceEventId;
        this.eventName = eventName;
        this.eventDesc = eventDesc;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(eventName);
    }

    @JsonCreator
    public static AdvanceEvent fromValue(String text) {
        for (AdvanceEvent b : AdvanceEvent.values()) {
            if (String.valueOf(b.eventName).equals(text)) {
                return b;
            }
        }
        throw getEventError("Unknown advance-event.");
    }

    private static BusinessLogicException getEventError(String message) {
        return ErrorUtils.getInternalError("Advance-event internal error: " + message);
    }
}
