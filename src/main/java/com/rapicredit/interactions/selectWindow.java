package com.rapicredit.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.WebDriver;

public class selectWindow implements Interaction {

    private final String handle;

    public selectWindow(String handle) {
        this.handle = handle;
    }

    public static selectWindow named(String handle) {
        return Tasks.instrumented(selectWindow.class, handle);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        driver.switchTo().window(handle);
    }

    @Override
    public String toString() {
        return "SelectWindow: " + handle;
    }
}

