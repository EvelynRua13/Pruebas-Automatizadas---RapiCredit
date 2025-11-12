package com.rapicredit.questions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.WebDriver;
import com.rapicredit.userInterface.registerPage;

public class registerIndicador implements Question<Boolean> {

    private final String expectedSubstring;

    public registerIndicador(String expectedSubstring) {
        this.expectedSubstring = expectedSubstring;
    }

    public static registerIndicador whenUrlContains(String expectedSubstring) {
        return new registerIndicador(expectedSubstring);
    }

    public static registerIndicador whenUrlContainsDefault() {
        return new registerIndicador(null);
    }

    @Override
    public Boolean answeredBy(Actor actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        String expected = this.expectedSubstring;
        if (expected == null || expected.isBlank()) {
            String prop = System.getProperty("register.success.url");
            expected = (prop != null && !prop.isBlank()) ? prop : registerPage.URL_AFTER_REGISTER;
        }
        String current = driver.getCurrentUrl();
        return current != null && current.contains(expected);
    }

    @Override
    public String toString() {
        return "RegisterIndicador checks URL contains '" + (expectedSubstring == null ? "<default>" : expectedSubstring) + "'";
    }
}
