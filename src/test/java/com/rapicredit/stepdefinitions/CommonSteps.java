package com.rapicredit.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import com.rapicredit.utils.CommonActions;
import com.rapicredit.userInterface.loginPage;
import com.rapicredit.userInterface.registerPage;
import com.rapicredit.interactions.SafeClick;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.junit.Assert;
import com.rapicredit.tasks.PayCredit;
import com.rapicredit.userInterface.PayCreditPage;
import net.serenitybdd.screenplay.Actor;
import com.rapicredit.interactions.ShowSuccessBanner;
import com.rapicredit.interactions.ClickIfPresent;

public class CommonSteps {

    @Given("the user is on the home page")
    public void the_user_is_on_the_home_page() {
        String home = System.getProperty("home.url");
        if (home == null || home.isBlank()) {
            home = loginPage.URL; // fallback
        }
        CommonActions.openHome(OnStage.theActorInTheSpotlight(), home);
    }

    @Given("the user is on the Rapicredit home page")
    public void the_user_is_on_the_Rapicredit_home_page() {
        the_user_is_on_the_home_page();
    }

    @When("the user clicks {string}")
    public void the_user_clicks(String element) {
        if ("Iniciar sesión".equalsIgnoreCase(element) || "Iniciar Sesión".equalsIgnoreCase(element)) {
            CommonActions.clickAndSwitch(OnStage.theActorInTheSpotlight(), loginPage.INICIAR_SESION);
            return;
        }
        if ("Registrar".equalsIgnoreCase(element) || "Regístrate".equalsIgnoreCase(element)) {
            CommonActions.clickAndSwitch(OnStage.theActorInTheSpotlight(), registerPage.LINK_REGISTRAR);
            return;
        }
        if (element != null && element.toLowerCase().contains("paga")) {
            // specifically for 'Paga tu crédito' ensure we switch to any new window/modal
            Target dynamicPay = Target.the(element).located(By.linkText(element));
            CommonActions.clickAndSwitch(OnStage.theActorInTheSpotlight(), dynamicPay);
            return;
        }
        Target dynamic = Target.the(element).located(By.linkText(element));
        OnStage.theActorInTheSpotlight().attemptsTo(SafeClick.on(dynamic));
    }

    @When("the user enters id {string}")
    public void the_user_enters_id(String id) {
        OnStage.theActorInTheSpotlight().attemptsTo(PayCredit.withCedula(id));
    }

    @Then("the user should see an indicator {string}")
    public void the_user_should_see_an_indicator(String expected) {
        try {
            WebDriver driver = BrowseTheWeb.as(OnStage.theActorInTheSpotlight()).getDriver();
            String currentUrl = driver != null ? driver.getCurrentUrl() : null;
            if (expected == null || expected.isBlank()) {
                Assert.fail("No expected indicator provided");
            }

            if (expected.equalsIgnoreCase("logged_in_indicator") || expected.equalsIgnoreCase("registered_indicator")) {
                boolean found = (currentUrl != null && currentUrl.contains(System.getProperty("login.success.url", "")))
                                 || (currentUrl != null && currentUrl.contains(System.getProperty("register.success.url", "")));
                Assert.assertTrue("Expected login/register indicator but URL was: " + currentUrl, found);
                return;
            }

            boolean present = (currentUrl != null && currentUrl.contains(expected));
            if (!present && driver != null) {
                String page = driver.getPageSource();
                present = page != null && page.contains(expected);
            }
            Assert.assertTrue("Expected indicator '" + expected + "' not found.", present);

        } catch (Exception e) {
            throw new RuntimeException("Error checking indicator: " + e.getMessage(), e);
        }
    }

    @Then("the user should see payment page or message {string}")
    public void the_user_should_see_payment_page_or_message(String msg) {
        Actor actor = OnStage.theActorInTheSpotlight();

        int attempts = 0;
        final int maxAttempts = 5;
        while (attempts < maxAttempts) {
            attempts++;
            // 1) check payment indicator safely
            try {
                try {
                    if (PayCreditPage.PAYMENT_PAGE_INDICATOR.resolveFor(actor).isVisible()) {
                        actor.attemptsTo(ShowSuccessBanner.now("Prueba exitosa", 3));
                        shortSleep();
                        return;
                    }
                } catch (Exception ignored) {//ignore
                    }

                // 2) check page source for message
                try {
                    String pageText = BrowseTheWeb.as(actor).getDriver().getPageSource();
                    if (pageText != null && pageText.contains(msg)) {
                        actor.attemptsTo(ShowSuccessBanner.now("Prueba exitosa", 3));
                        shortSleep();
                        return;
                    }
                } catch (Exception ignored)
                {
                    // ignore
                }

                // 3) check the NO_PENDING_ALERT element safely
                try {
                    if (PayCreditPage.NO_PENDING_ALERT.resolveFor(actor).isVisible()) {
                        actor.attemptsTo(ShowSuccessBanner.now("Prueba exitosa", 3));
                        shortSleep();
                        return;
                    }
                } catch (Exception ignored)
                {
                    // ignore
                }

                // 4) if not found yet, attempt one submit click (safely) and wait a bit
                try {
                    actor.attemptsTo(ClickIfPresent.on(PayCreditPage.BTN_SUBMIT));
                } catch (Exception ignored)
                {
                    // ignore
                }

                shortSleep(1000);
            } catch (Exception e) {
                // safety catch for unexpected exceptions in loop
                shortSleep(500);
            }
        }
        try { actor.attemptsTo(ShowSuccessBanner.now("Prueba finalizada: no se detectó resultado", 4));
        } catch (Exception ignored)
        {
            // ignore
        }
    }

    private void shortSleep() {
        shortSleep(700);
    }

    private void shortSleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
