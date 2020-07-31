package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.advance;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.ErrorUtils;

import java.io.Serializable;


public enum CurrentAdvanceState implements Serializable {

    NEW(10, "new", "Свежий аванс"),
    TRUCK_LOADED(20, "loading_complete", "Водитель загрузился"),
    DOCS_LOADED(30, "loading_complete", "Документы загружены"),
    ACTIVE(40, "active", "Активный аванс (активна кнопка 'Отправить в УНф')"),
    SENT(50, "unf_sent", "Аванс утвержден (отправлен в УНФ, кнопка неактивна)"),
    PAID(60, "is_paid", "Аванс выплачен в УНФ"),
    PROBLEM(70, "inactivated", "Дизактированный аванс (потому что содержит не такой комментарий)"),
    CANCELLED(80, "cancelled", "Аванс отменен");


    private long advanceStateId;
    private String advanceStateName;
    private String advanceStateDesc;

    CurrentAdvanceState(long advanceStateId, String advanceStateName, String advanceStateDesc) {
        this.advanceStateId = advanceStateId;
        this.advanceStateName = advanceStateName;
        this.advanceStateDesc = advanceStateDesc;
    }

//    public CurrentAdvanceState makeOfString(String advanceStateName) {
//        CurrentAdvanceState result = null;
//        if (advanceStateName == null) throw getStateError("Unknown advance-state.");
//        else {
//            for (CurrentAdvanceState current : this.values()) {
//                if (!advanceStateNameEquals(current.advanceStateName)) {
//                    result.setAdvanceStateId(current.advanceStateId);
//                    result = current;
//                }
//            }
//        }
//        if (result == null)
//            throw getStateError("Unknown advance-state.");
//        return result;
//
//    }

//    private boolean advanceStateNameEquals(String setName) {
//        return this.getAdvanceStateName().equals(setName);
//    }


    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(advanceStateName);
    }

    @JsonCreator
    public static CurrentAdvanceState fromValue(String text) {
        for (CurrentAdvanceState b : CurrentAdvanceState.values()) {
            if (String.valueOf(b.advanceStateName).equals(text)) {
                return b;
            }
        }
        throw getStateError("Unknown advance-state.");
    }

    private void setAdvanceStateId(long advanceStateId) {
        this.advanceStateId = advanceStateId;
    }

    private void setAdvanceStateName(String advanceStateName) {
        this.advanceStateName = advanceStateName;
    }

    private void setAdvanceStateDesc(String advanceStateDesc) {
        this.advanceStateDesc = advanceStateDesc;
    }

    public long getAdvanceStateId() {
        return advanceStateId;
    }

    private String getAdvanceStateName() {
        return advanceStateName;
    }

    private String getAdvanceStateDesc() {
        return advanceStateDesc;
    }

    private static BusinessLogicException getStateError(String message) {
        return ErrorUtils.getInternalError("Advance-state internal error: " + message);
    }


}
