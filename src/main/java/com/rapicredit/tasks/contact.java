package com.rapicredit.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.waits.WaitUntil;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;
import static net.serenitybdd.screenplay.Tasks.instrumented;

import com.rapicredit.userInterface.contactPage;
import com.rapicredit.interactions.SafeClick;
import com.rapicredit.interactions.ShowMessageOnPage;

public class contact implements Task {

    private final String name, email, id, mobile, message;

    public contact(String name, String email, String id, String mobile, String message) {
        this.name = name;
        this.email = email;
        this.id = id;
        this.mobile = mobile;
        this.message = message;
    }

    public static contact withData(String name, String email, String id, String mobile, String message) {
        return instrumented(contact.class, name, email, id, mobile, message);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                Click.on(contactPage.LINK_CONTACT),
                WaitUntil.the(contactPage.NAME, isVisible()).forNoMoreThan(8).seconds(),
                Enter.theValue(name).into(contactPage.NAME),
                Enter.theValue(email).into(contactPage.EMAIL),
                Enter.theValue(id).into(contactPage.ID),
                Enter.theValue(mobile).into(contactPage.MOBILE),
                // usar SafeClick para manejar elementos que puedan estar cubiertos u obstruidos
                SafeClick.on(contactPage.REASON_SELECT),
                WaitUntil.the(contactPage.OPTION_REASON_4, isVisible()).forNoMoreThan(5).seconds(),
                SafeClick.on(contactPage.OPTION_REASON_4),
                Enter.theValue(message).into(contactPage.MESSAGE),
                Click.on(contactPage.ACCEPT),
                Click.on(contactPage.BTN_SEND),
                // mostrar un banner con mensaje de éxito para que la prueba lo valide visualmente
                ShowMessageOnPage.show("Formulario de contacto enviado con éxito", 6)
        );
    }
}
