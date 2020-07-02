package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.advance;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import org.springframework.http.HttpStatus;

import java.io.Serializable;


public enum CurrentAdvanceState implements Serializable {
    NOTIFIED(10, "notified", "Было уведомление"), //New created --> to Active
    AUTO_NOTIFIED(11, "delay_notified", "Было отложенное уведомление"), //New created --> to Active
    SMS_SENT(12, "sent_sms", "Было отложенное уведомление"), //New created --> to Active
    EMAIL_SENT(13, "sent_email", "Было отложенное уведомление"), //New created --> to Active

    NEW(1, "new", "Свежий аванс"), //New created --> to Active
    AUTO_NEW(2, "auto", "Свежий авто-аванс"),  //New auto-created

    LOADING_COMPLETE(3, "truck_loaded", "Загрузка машины завершена"), //

    ACTIVE(4,"active", "Активный аванс (активна кнопка)"),  //Active button --> not Problem

    PROBLEM_ADVANCE(5, "dis_activated", "Дизактированный аванс (потому что содержит не такой комментарий)"),

    CANCELLED(6, "cancelled", "Аванс отменен"), //Succefully_cancel

    TRY_SEND_UNF(7, "try_unf_send" ,"Отправляли в УНФ (нажимали кнопку?)"), // try sent to unf (button pushed. but disActive?)
    SENT_TO_UNF(4, "unf_sent" ,"Отправляли в УНФ"), // Advance Already Sent_in_Unf. When already Send, button is unActive
    APPROVED(5, "approved", "Аванс утвержден"),  //Succefully_approved --> download documents

    PAID(8, "is_paid", "Аванс выплачен в УНФ"), //Succefully_PAID - end

    COMPLETE(9, "complete", "Завершен"); //Succefully_PAID - end




    private long advanceStateId;
    private String advanceStateName;
    private String advanceStateDesc;

    CurrentAdvanceState(long advanceStateId, String advanceStateName, String advanceStateDesc) {
        this.advanceStateId = advanceStateId;
        this.advanceStateName = advanceStateName;
        this.advanceStateDesc = advanceStateDesc;
    }

    public CurrentAdvanceState makeOfString(String advanceStateName) {
        CurrentAdvanceState result = null;
        if (advanceStateName == null) throw new BusinessLogicException(HttpStatus.CONFLICT,new Error());
        else {
            for (CurrentAdvanceState current : this.values()) {
                if (!advanceStateNameEquals(current.advanceStateName)) {
                    result.setAdvanceStateId(current.advanceStateId);
                    result = current;
                }
            }
        }
        if (result == null)
            throw new BusinessLogicException(HttpStatus.CONFLICT,new Error());
        return result;

    }

    private boolean advanceStateNameEquals(String setName){
        return this.getAdvanceStateName().equals(setName);
    }





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
        throw new IllegalArgumentException("Unexpected value '" + text + "'");
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


}
