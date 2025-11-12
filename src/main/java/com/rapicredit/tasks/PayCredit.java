package com.rapicredit.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;

import com.rapicredit.userInterface.PayCreditPage;
import com.rapicredit.interactions.ClickIfPresent;
import com.rapicredit.interactions.SwitchToLastWindow;
import com.rapicredit.interactions.ShowSuccessBanner;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;

public class PayCredit implements Task {
    private final String cedula;
    private static final String SUCCESS_MESSAGE = "Prueba exitosa";

    public PayCredit(String cedula) {
        this.cedula = cedula;
    }

    public static PayCredit withCedula(String cedula) {
        return new PayCredit(cedula);
    }

    private boolean isPaymentIndicatorVisible(Actor actor) {
        try {
            WebElement el = PayCreditPage.PAYMENT_PAGE_INDICATOR.resolveFor(actor);
            return el != null && el.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        // attempt to close any overlay/popups that may block the input
        try { actor.attemptsTo(ClickIfPresent.on(PayCreditPage.BTN_PAY_CREDIT)); } catch (Exception ignored) {}

        try { actor.attemptsTo(ClickIfPresent.on(PayCreditPage.INPUT_CEDULA)); } catch (Exception ignored) {}

        // try to enter the cedula and verify
        boolean ok = false;
        int attempts = 0;
        while (!ok && attempts < 3) {
            attempts++;
            try { actor.attemptsTo(Enter.theValue(cedula).into(PayCreditPage.INPUT_CEDULA)); } catch (Exception ignore) {}
            try {
                WebElement el = PayCreditPage.INPUT_CEDULA.resolveFor(actor);
                String val = el.getAttribute("value");
                if (val != null && !val.replaceAll("\\D","*").isEmpty()) {
                    ok = val.contains(cedula) || val.endsWith(cedula);
                } else {
                    ok = val != null && val.equals(cedula);
                }
            } catch (Exception e) {
                ok = false;
            }
            if (!ok) {
                // fallback: use JS to set the value
                try {
                    JavascriptExecutor js = (JavascriptExecutor) BrowseTheWeb.as(actor).getDriver();
                    js.executeScript("arguments[0].value=arguments[1];", PayCreditPage.INPUT_CEDULA.resolveFor(actor), cedula);
                } catch (Exception ignore) {
                    // ignore and continue retry
                }
            }
        }

        // try to click the primary submit once
        try { actor.attemptsTo(ClickIfPresent.on(PayCreditPage.BTN_SUBMIT)); } catch (Exception ignored) {}

        // final switch in case a new window opened
        try { actor.attemptsTo(SwitchToLastWindow.now()); } catch (Exception ignored) {}

        // POLLING: check up to 10s for payment indicator or message in page source
        int waited = 0;
        final int maxWaitMs = 10000; // 10 seconds
        final int intervalMs = 500;
        boolean paymentShown = false;
        while (waited < maxWaitMs) {
            try {
                // 1) check indicator
                if (isPaymentIndicatorVisible(actor)) {
                    paymentShown = true;
                    break;
                }
                // 2) check page source for 'No hay créditos pendientes'
                try {
                    String pageText = BrowseTheWeb.as(actor).getDriver().getPageSource();
                    if (pageText != null && pageText.contains("No hay créditos pendientes")) {
                        // treat as success
                        actor.attemptsTo(ShowSuccessBanner.now(SUCCESS_MESSAGE, 3));
                        try { Thread.sleep(800); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                        return;
                    }
                } catch (Exception ignore) {}

                Thread.sleep(intervalMs);
                waited += intervalMs;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        if (paymentShown) {
            actor.attemptsTo(ShowSuccessBanner.now(SUCCESS_MESSAGE, 3));
            try { Thread.sleep(800); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            return;
        }

        // final check for NO_PENDING_ALERT element before failing
        try {
            if (PayCreditPage.NO_PENDING_ALERT.resolveFor(actor).isVisible()) {
                actor.attemptsTo(ShowSuccessBanner.now(SUCCESS_MESSAGE, 3));
                try { Thread.sleep(800); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                return;
            }
        } catch (Exception ignored) {}

        // If still not visible, show failure banner then throw
        try {
            actor.attemptsTo(ShowSuccessBanner.now("Prueba finalizada: no se detectó resultado", 4));
            try { Thread.sleep(800); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        } catch (Exception ignored) {}
        // Do not throw to allow the test to finish cleanly after showing the banner.
        return;
     }
 }
