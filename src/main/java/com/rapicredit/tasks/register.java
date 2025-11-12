package com.rapicredit.tasks;


import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.actions.Scroll;
import net.serenitybdd.screenplay.actions.JavaScriptClick;
import net.serenitybdd.screenplay.waits.WaitUntil;
import net.serenitybdd.screenplay.matchers.WebElementStateMatchers;

import com.rapicredit.userInterface.registerPage;

public class register implements Task {

    private final String idNumber;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String reEmail;
    private final String mobile;
    private final String password;
    private final String rePassword;

    public register(String idNumber, String firstName, String lastName, String email, String reEmail, String mobile, String password, String rePassword) {
        this.idNumber = idNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.reEmail = reEmail;
        this.mobile = mobile;
        this.password = password;
        this.rePassword = rePassword;
    }

    public static register withData(String idNumber, String firstName, String lastName, String email, String reEmail, String mobile, String password, String rePassword) {
        return new register(idNumber, firstName, lastName, email, reEmail, mobile, password, rePassword);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        // seleccionar tipo de identificación (simple click + elegir opción)
        actor.attemptsTo(
                Click.on(registerPage.IDENTIFICATION_TYPE),
                WaitUntil.the(registerPage.OPTION_IDENTIFICATION, WebElementStateMatchers.isClickable()).forNoMoreThan(8).seconds(),
                Click.on(registerPage.OPTION_IDENTIFICATION),

                // completar campos
                Enter.theValue(idNumber).into(registerPage.IDENTIFICATION_NUMBER),
                Enter.theValue(firstName).into(registerPage.FIRST_NAME),
                Enter.theValue(lastName).into(registerPage.LAST_NAME),
                Enter.theValue(email).into(registerPage.EMAIL),
                Enter.theValue(reEmail).into(registerPage.REEMAIL),
                Enter.theValue(mobile).into(registerPage.MOBILE),
                Enter.theValue(password).into(registerPage.PASSWORD),
                Enter.theValue(rePassword).into(registerPage.REPASSWORD)
        );

        // aceptar términos y tratamiento y desplazarse al botón
        actor.attemptsTo(
                // asegurar que los elementos estén visibles y usar JavaScriptClick si el elemento no es interactuable normalmente
                Scroll.to(registerPage.TERMS),
                JavaScriptClick.on(registerPage.TERMS),
                Scroll.to(registerPage.DATA_TREATMENT),
                JavaScriptClick.on(registerPage.DATA_TREATMENT),

                // asegurar que el botón sea visible y clickeable
                Scroll.to(registerPage.BTN_REGISTER),
                WaitUntil.the(registerPage.BTN_REGISTER, WebElementStateMatchers.isClickable()).forNoMoreThan(8).seconds()
        );
    }
}
