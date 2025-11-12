package com.rapicredit.stepdefinitions;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.waits.WaitUntil;
import net.serenitybdd.screenplay.matchers.WebElementStateMatchers;
import com.rapicredit.userInterface.faqPage;
import com.rapicredit.interactions.SafeClick;
import com.rapicredit.interactions.CloseOverlays;
import com.rapicredit.tasks.SearchFaq;
import com.rapicredit.questions.FaqQuestion;
import org.junit.Assert;

public class FaqSteps {

    @When("the user opens the \"Preguntas frecuentes\" section")
    public void the_user_opens_the_preguntas_frecuentes_section() {
            OnStage.theActorInTheSpotlight().attemptsTo(
                    // attempt to close overlays first in case a popup covers the page
                    CloseOverlays.now(),
                    // esperar visibilidad en lugar de isClickable para evitar bloqueos por overlays
                    WaitUntil.the(faqPage.FAQ_URL, WebElementStateMatchers.isVisible()).forNoMoreThan(15).seconds(),
                    SafeClick.on(faqPage.FAQ_URL));

            // después del click, esperar que se cargue el input de búsqueda en la página de FAQ
            try {
                OnStage.theActorInTheSpotlight().attemptsTo(
                        WaitUntil.the(faqPage.INPUT_SEARCH, WebElementStateMatchers.isVisible()).forNoMoreThan(12).seconds()
                );
            } catch (Exception e) {
                // si no aparece, intentar cerrar overlays y reintentar brevemente
                OnStage.theActorInTheSpotlight().attemptsTo(CloseOverlays.now());
                try {
                    OnStage.theActorInTheSpotlight().attemptsTo(
                            WaitUntil.the(faqPage.INPUT_SEARCH, WebElementStateMatchers.isVisible()).forNoMoreThan(8).seconds()
                    );
                } catch (Exception ex) {
                    // no forzar fallo aquí; el siguiente paso (SearchFaq) hará la espera y fallará con mensaje claro
                }
            }
    }

    @When("the user searches for keyword {string}")
    public void the_user_searches_for_keyword(String keyword) {
        // perform search (task will handle overlays and navigation)
        OnStage.theActorInTheSpotlight().attemptsTo(
                SearchFaq.forKeyword(keyword)
        );

        // ensure overlays closed after search
        OnStage.theActorInTheSpotlight().attemptsTo(CloseOverlays.now());

        // Wait briefly for results/accordion to appear and assert using the Question
        try {
            OnStage.theActorInTheSpotlight().attemptsTo(
                    WaitUntil.the(faqPage.INPUT_SEARCH, WebElementStateMatchers.isVisible()).forNoMoreThan(8).seconds()
            );
        } catch (Exception ignored) { /* best-effort wait */ }

        boolean found = OnStage.theActorInTheSpotlight().asksFor(FaqQuestion.resultsContain(keyword));
        if (!found) {
            throw new AssertionError("FAQ flow failed: accordion/result for keyword '" + keyword + "' not found");
        }
    }

    @Then("results related to {string} should be visible")
    public void results_related_to_should_be_visible(String keyword) {
        boolean found = OnStage.theActorInTheSpotlight().asksFor(FaqQuestion.resultsContain(keyword));
        Assert.assertTrue("Expected keyword not found in FAQ results: " + keyword, found);
    }
}
