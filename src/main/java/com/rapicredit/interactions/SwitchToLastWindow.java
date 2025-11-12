package com.rapicredit.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.WebDriver;

import java.util.Set;

public class SwitchToLastWindow implements Interaction {
    public static SwitchToLastWindow now() { return new SwitchToLastWindow(); }

    @Override
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        try {
            String current = driver.getWindowHandle();
            Set<String> handles = driver.getWindowHandles();
            if (handles.size() > 1) {
                for (String h : handles) {
                    if (!h.equals(current)) {
                        driver.switchTo().window(h);
                    }
                }
            }
        } catch (Exception e) {
            // ignore if not possible
        }
    }
}
