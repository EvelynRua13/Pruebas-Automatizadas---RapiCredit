package com.rapicredit.questions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

import com.rapicredit.userInterface.PayCreditPage;

public class CreditPending implements Question<Boolean> {

    public static CreditPending isVisible() {
        return new CreditPending();
    }

    @Override
    public Boolean answeredBy(Actor actor) {
        try {
            return PayCreditPage.PAYMENT_PAGE_INDICATOR.resolveFor(actor).isVisible();
        } catch (Exception e) {
            return false;
        }
    }
}
