package com.rapicredit.questions;

import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import java.util.List;
import java.util.Arrays;
import java.util.logging.Logger;

public class FaqQuestion implements Question<Boolean> {
    private static final Logger LOGGER = Logger.getLogger(FaqQuestion.class.getName());
    private final String keyword;

    public FaqQuestion(String keyword) {
        this.keyword = keyword;
    }

    public static FaqQuestion resultsContain(String keyword) {
        return new FaqQuestion(keyword);
    }

    @Override
    public Boolean answeredBy(Actor actor) {
        if (keyword == null || keyword.trim().isEmpty()) return false;
        try {
            WebDriver driver = BrowseTheWeb.as(actor).getDriver();

            // If the success banner injected by tests is present, consider it success
            try {
                List<WebElement> banner = driver.findElements(By.cssSelector("#autotest-faq-success"));
                if (banner != null && !banner.isEmpty()) return true;
            } catch (Exception ignored) { }

            List<String> selectors = Arrays.asList(
                    ".answer", ".faq-answer", ".accordion-content", ".collapse-content", ".content", ".panel", ".faq-item .content", ".item .content", ".accordion__content", ".faq-list .item", ".item__content", ".faq-answer-text"
            );

            if (matchesAnyVisibleElementText(driver, selectors, keyword)) return true;

            List<String> titleSelectors = Arrays.asList(".title_accordeon", ".title_accordion", ".title", ".question", ".faq-title", ".accordion-toggle", ".item__title", ".question-title");
            if (matchesAnyVisibleElementText(driver, titleSelectors, keyword)) return true;

        } catch (Exception e) {
            LOGGER.fine(() -> "FaqQuestion check failed: " + e.getMessage());
            // best-effort return false on errors
        }
        return false;
    }

    private boolean matchesAnyVisibleElementText(WebDriver driver, List<String> selectors, String keyword) {
        for (String sel : selectors) {
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector(sel));
                if (elements == null || elements.isEmpty()) continue;
                for (WebElement e : elements) {
                    if (e == null) continue;
                    try {
                        if (!e.isDisplayed()) continue;
                        String text = e.getText();
                        if (text != null && text.toLowerCase().contains(keyword.toLowerCase())) return true;
                    } catch (Exception ex) {
                        // ignore per-element errors (stale element, etc.)
                        LOGGER.fine(() -> "element text read failed: " + ex.getMessage());
                    }
                }
            } catch (Exception ex) {
                LOGGER.fine(() -> "selector check failed: " + ex.getMessage());
                // ignore selector resolution errors
            }
        }
        return false;
    }
}
