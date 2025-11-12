package com.rapicredit.stepdefinitions;

import io.cucumber.java.Before;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;

public class PayCreditStep {

    @Before
    public void setStage() {
        if (OnStage.theActorCalled("User") == null) {
            OnStage.setTheStage(new OnlineCast());
            OnStage.theActorCalled("User");
        } else {
            OnStage.setTheStage(new OnlineCast());
            OnStage.theActorCalled("User");
        }
    }

}
