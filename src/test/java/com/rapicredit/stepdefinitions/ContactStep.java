package com.rapicredit.stepdefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;
import net.serenitybdd.screenplay.Actor;
import com.rapicredit.tasks.contact;
import com.rapicredit.questions.contactIndicator;
import com.rapicredit.interactions.ShowMessageOnPage;

public class ContactStep {

    @Before
    public void setTheStage() {
        OnStage.setTheStage(new OnlineCast());
        OnStage.theActorCalled("User");
    }

    @When("the user submits the contact form with the following data")
    public void the_user_submits_contact_form(io.cucumber.datatable.DataTable table) {
        java.util.List<java.util.Map<String,String>> rows = table.asMaps(String.class, String.class);
        if (rows.isEmpty()) return;
        java.util.Map<String,String> data = rows.get(0);

        // Preferir claves sencillas en el feature: Nombre, Email, Cedula, Celular, Mensaje
        String name = data.getOrDefault("Nombre", data.getOrDefault("name", ""));
        String email = data.getOrDefault("Email", data.getOrDefault("email", ""));
        String id = data.getOrDefault("Cedula", data.getOrDefault("cedula", ""));
        String mobile = data.getOrDefault("Celular", data.getOrDefault("celular", ""));
        String message = data.getOrDefault("Mensaje", data.getOrDefault("mensaje", "Prueba Automatizada"));

        OnStage.theActorInTheSpotlight().attemptsTo(
                contact.withData(name, email, id, mobile, message)
        );
    }

    @Then("the contact message should be sent successfully")
    public void the_contact_message_should_be_sent_successfully() {
        Actor actor = OnStage.theActorInTheSpotlight();
        boolean ok = contactIndicator.isPresent().answeredBy(actor);
        if (ok) {
            actor.attemptsTo(ShowMessageOnPage.show("Mensaje de contacto enviado correctamente",5));
        } else {
            actor.attemptsTo(ShowMessageOnPage.show("Fallo en envío de contacto",5));
            throw new AssertionError("Contacto no fue enviado: indicador no encontrado");
        }
    }
}
