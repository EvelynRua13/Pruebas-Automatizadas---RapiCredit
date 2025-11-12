package com.rapicredit.questions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

class loginIndicator implements Question<Boolean> {

    private final String expectedText;

    public loginIndicator(String expectedText) {
        this.expectedText = expectedText;
    }

    public static loginIndicator displays(String expectedText) {
        return new loginIndicator(expectedText);
    }

    @Override
    public Boolean answeredBy(Actor actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        // Busca cualquier elemento que contenga el texto esperado (normalize-space para evitar problemas con espacios)
        return !driver.findElements(By.xpath("//*[contains(normalize-space(.), '" + expectedText + "')]")).isEmpty();
    }

    @Override
    public String toString() {
        return "LoginIndicator displays '" + expectedText + "'";
    }
}
