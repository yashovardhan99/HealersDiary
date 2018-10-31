package com.yashovardhan99.healersdiary.objects;

import java.text.NumberFormat;

/**
 * Created by Yashovardhan99 on 25-06-2018 as a part of HealersDiary.
 */
public class PaymentSnapshot {
    public String date, amount, Uid;
    public PaymentSnapshot(){
        date = "";
        amount = NumberFormat.getCurrencyInstance().format(0);
        Uid = "";
    }

    public String getDate() {
        return date;
    }

    public String getAmount() {
        return amount;
    }

    public String getUid() {
        return Uid;
    }
}
