package com.rapicredit.questions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;

public class contactIndicator implements Question<Boolean> {

    public static contactIndicator isPresent() { return new contactIndicator(); }

    @Override
    public Boolean answeredBy(Actor actor) {
        try {
            WebDriver driver = BrowseTheWeb.as(actor).getDriver();
            if (driver == null) return false;

            boolean containsText = driver.getPageSource().contains("Gracias") || driver.getPageSource().contains("Enviado")
                    || driver.getPageSource().contains("Hemos recibido");

            boolean hasBanner = false;
            try {
                hasBanner = !driver.findElements(By.xpath("//*[contains(@id,'serenity-banner') or contains(@class,'alert-success') or contains(@class,'success')]")).isEmpty();
            } catch (Exception ignored) {}

            return containsText || hasBanner;
        } catch (Exception e) {
            return false;
        }
    }
}
