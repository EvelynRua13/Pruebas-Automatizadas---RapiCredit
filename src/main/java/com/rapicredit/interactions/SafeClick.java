package com.rapicredit.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.ElementClickInterceptedException;

import java.util.List;

public class SafeClick implements Interaction {

    private final Target target;

    public SafeClick(Target target) {
        this.target = target;
    }

    public static SafeClick on(Target target) {
        return new SafeClick(target);
    }

    @Override
    public <T extends Actor> void performAs(T actor) throws RuntimeException {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        List<WebElementFacade> elements = target.resolveAllFor(actor);
        if (elements.isEmpty()) {
            throw new RuntimeException("Element not found for SafeClick: " + target.getName());
        }

        WebElementFacade element = elements.get(0);

        try {
            // wait until clickable and use WebElementFacade click (Serenity wrapper)
            try {
                element.waitUntilClickable();
            } catch (Exception ignored) { }

            // ensure element is visible and scrolled into view
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior:'auto',block:'center',inline:'center'});", element);
            } catch (Exception ignored) { }

            element.click();

        } catch (ElementClickInterceptedException e) {
            // if intercepted, try JS click as fallback
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            } catch (Exception ex) {
                throw new RuntimeException("Unable to click element for SafeClick (JS fallback failed): " + target.getName(), ex);
            }
        } catch (NoSuchElementException e) {
            throw new RuntimeException("Element not found for SafeClick: " + target.getName(), e);
        } catch (Exception e) {
            // final fallback: direct JS click
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            } catch (Exception ex) {
                throw new RuntimeException("SafeClick failed for: " + target.getName(), ex);
            }
        }
    }
}
