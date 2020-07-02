package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.email;

 public enum EmailUsersModes {

    // Only contact mode is defaultMode:
    //
    ONLY_CONTACT_MODE("Only one Advance user emaild."),


    // Отправляет всем "пользователям" аванса уведомление по и-мейлу.

    // Если нужно уведомить об авансе группу пользователей, связанных с авансом: типа "Авторы", "Админы", "Водители",
    // ,,, пеймент-контракторы, унф, акцес-юзаеры - нужно написать свой мод.

    // Всех ставит в реплаи??
    //
    ALL_USERS_MODE("All Advance users emailed."),
    //
    REPLY("Only 'reply' sections set for only user.");




    EmailUsersModes(String s) {
        setMode(s);

        // or setEmailsUsersModes?
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    //short id;
    private String mode;


    //[]?
    void setEmailsUsersModes(String emailMessagingModes) {
        this.mode = emailMessagingModes;
    }

//    void setEmailsUsersModes(String emailMessagingModes) {
//        this.mode = emailMessagingModes;
//    }

}
