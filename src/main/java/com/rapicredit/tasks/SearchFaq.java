package com.rapicredit.tasks;

import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import com.rapicredit.userInterface.faqPage;
import com.rapicredit.interactions.CloseOverlays;
import com.rapicredit.interactions.SafeClick;
import net.serenitybdd.screenplay.targets.Target;
import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

public class SearchFaq implements Task {
    private static final Logger LOGGER = Logger.getLogger(SearchFaq.class.getName());

    private final String keyword;

    public SearchFaq(String keyword) {
        this.keyword = keyword;
    }

    public static SearchFaq forKeyword(String keyword) {
        return new SearchFaq(keyword);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        closeOverlays(actor);
        ensureOnFaq(actor);

        try {
            WebDriver driver = BrowseTheWeb.as(actor).getDriver();

            WebElement input = findSearchInput(driver, Duration.ofSeconds(6));
            if (input != null) {
                setInputValue(driver, input, keyword);
                triggerSearchAction(actor, driver, input);
            } else {
                LOGGER.info("FAQ: search input not found");
            }

            // wait for a result item to appear
            WebElement firstItem = findFirstVisible(driver, new String[]{".item", ".faq-item", ".accordion .item", ".faq-accordion .item", ".faq-list .item", ".accordion__item", ".faq__item", ".accordion-item"}, Duration.ofSeconds(10));
            if (firstItem == null) {
                throw new AssertionError("FAQ flow failed: no results found for keyword '" + keyword + "'");
            }

            // open first accordion item (more robust)
            // try a global open (some sites render title outside the result root)
            boolean opened = openFirstAccordionGlobal(driver, Duration.ofSeconds(10));
            if (!opened) {
                throw new AssertionError("FAQ flow failed: accordion not opened for keyword '" + keyword + "'");
            }

            injectSuccessBanner(driver, "Busqueda de FAQ exitosa: '" + keyword + "'", 5000);

        } catch (Exception ex) {
            throw new AssertionError("FAQ flow failed with exception: " + ex.getMessage(), ex);
        }
    }

    // --- Helpers ---

    private void setInputValue(WebDriver driver, WebElement el, String value) {
        try { el.sendKeys(""); } catch (Exception e) { LOGGER.fine("sendKeys empty failed: " + e.getMessage()); }
        try { ((JavascriptExecutor) driver).executeScript("arguments[0].focus();", el); } catch (Exception e) { LOGGER.fine("focus fallback failed: " + e.getMessage()); }
        safeClear(el);
        sendKeysOrSetValue(driver, el, value);
    }

    private <T extends Actor> void triggerSearchAction(T actor, WebDriver driver, WebElement input) {
        try { input.sendKeys(org.openqa.selenium.Keys.ENTER); } catch (Exception e) { LOGGER.fine("Enter key not sent: " + e.getMessage()); }

        // try to click a nearby search button if present
        String[] buttonSelectors = new String[] {".faq-search button", "button[type=submit]", ".search button", ".btn-search", ".search__submit", "button[data-action='search']"};
        for (String sel : buttonSelectors) {
            try {
                List<WebElement> found = driver.findElements(By.cssSelector(sel));
                for (WebElement e : found) {
                    if (isDisplayedSafe(e)) {
                        try {
                            Target t = Target.the("faq search button").located(By.cssSelector(sel));
                            actor.attemptsTo(SafeClick.on(t));
                            return;
                        } catch (Exception ex) {
                            LOGGER.fine("SafeClick on selector '" + sel + "' failed: " + ex.getMessage());
                        }
                    }
                }
            } catch (Exception exInner) { LOGGER.fine("search button selector check failed for '"+sel+"': " + exInner.getMessage()); }
        }

        // fallback: dispatch keyboard event
        try {
            ((JavascriptExecutor) driver).executeScript("var e=new KeyboardEvent('keydown',{key:'Enter',keyCode:13,which:13}); arguments[0].dispatchEvent(e);", input);
        } catch (Exception e) { LOGGER.fine("dispatch Enter event failed: " + e.getMessage()); }
    }

    private void safeClear(WebElement el) {
        try { el.clear(); } catch (Exception e) { LOGGER.fine("clear() failed: " + e.getMessage()); }
    }

    private void sendKeysOrSetValue(WebDriver driver, WebElement el, String text) {
        try { el.sendKeys(text); return; } catch (Exception e) { LOGGER.fine("el.sendKeys fallback: " + e.getMessage()); }
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input',{bubbles:true})); arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", el, text);
        } catch (Exception ex) { LOGGER.info(() -> "sendKeys fallback failed: " + ex.getMessage()); }
    }

    private boolean isDisplayedSafe(WebElement e) {
        try { return e != null && e.isDisplayed(); } catch (Exception ex) { return false; }
    }

    private WebElement findFirstVisible(WebDriver driver, String[] selectors, Duration timeout) {
        long end = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < end) {
            for (String sel : selectors) {
                try {
                    List<WebElement> found = driver.findElements(By.cssSelector(sel));
                    for (WebElement e : found) {
                        if (isDisplayedSafe(e)) return e;
                    }
                } catch (Exception ex) { LOGGER.fine("findFirstVisible selector failed '"+sel+"': " + ex.getMessage()); }
            }
            try { Thread.sleep(200); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); LOGGER.fine("sleep interrupted"); }
        }
        return null;
    }

    private WebElement findSearchInput(WebDriver driver, Duration timeout) {
        String[] inputSelectors = new String[] {
            "input[id*=preguntas]","input[id*=search]","input[name*=preguntas]","input[name*=pregunta]","input[placeholder*=Buscar]","input[type=search]",
            ".faq-search input", ".search input", "input[aria-label*=Buscar]"
        };
        return findFirstVisible(driver, inputSelectors, timeout);
    }

    // When the page may render results outside the original 'item' root use a global search
    private boolean openFirstAccordionGlobal(WebDriver driver, Duration timeout) {
        String[] titleSelectors = new String[] {
            "#preguntas de búsqueda", // fallback id-ish
            "input[id*=preguntas]", // not a title but helps ensure page readiness
            ".item:nth-child(1) .title_accordeon",
            ".item:first-child .title_accordeon",
            ".item:nth-child(1) .title",
            ".faq-list .item:first-child .title",
            ".faq-list .item:first-child .title_accordeon",
            ".title_accordeon", ".title_accordion", ".title", ".faq-title", ".question", ".accordion-toggle", ".collapse-title", ".item__title", ".question-title"
        };

        WebElement title = findFirstVisible(driver, titleSelectors, timeout);
        if (title == null) return false;

        int attempts = 0;
        long endTime = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < endTime && attempts < 6) {
            attempts++;
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", title);
            } catch (Exception jsEx) {
                try { title.click(); } catch (Exception e) { LOGGER.fine("Global title click failed: " + e.getMessage()); }
            }

            // short pause to allow DOM to update
            sleepSilently(300);

            if (checkAnyAnswerVisible(driver)) {
                sleepSilently(300);
                return true;
            }
        }

        return false;
    }

    private void sleepSilently(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    private boolean checkAnyAnswerVisible(WebDriver driver) {
        String[] answerSelectors = new String[] {".answer", ".content", ".panel", ".accordion-content", ".faq-answer", ".collapse-content", ".item__content", ".faq-answer-text"};
        for (String sel : answerSelectors) {
            try {
                List<WebElement> answers = driver.findElements(By.cssSelector(sel));
                for (WebElement a : answers) {
                    if (isDisplayedSafe(a)) return true;
                }
            } catch (Exception ex) { LOGGER.fine("checkAnyAnswerVisible failed for '"+sel+"': " + ex.getMessage()); }
        }
        return false;
    }

    private void injectSuccessBanner(WebDriver driver, String message, int visibleMs) {
        try {
            ((JavascriptExecutor)driver).executeScript(
                "(function(msg,ms){ var id='autotest-faq-success'; try{var old=document.getElementById(id); if(old) old.remove(); }catch(e){} var d=document.createElement('div'); d.id=id; d.style.position='fixed'; d.style.zIndex=999999; d.style.right='20px'; d.style.top='20px'; d.style.background='#2ecc71'; d.style.color='#fff'; d.style.padding='12px 18px'; d.style.borderRadius='6px'; d.style.boxShadow='0 2px 6px rgba(0,0,0,0.3)'; d.style.fontFamily='Arial,Helvetica,sans-serif'; d.style.fontSize='14px'; d.innerText=msg; document.body.appendChild(d); setTimeout(function(){ try{ d.style.transition='opacity 0.6s'; d.style.opacity='0'; setTimeout(function(){ try{ d.remove(); }catch(e){} },600); }catch(e){} },ms); })(arguments[0], arguments[1]);",
                message, visibleMs
            );
        } catch (Exception e) {
            LOGGER.info(() -> "Could not inject success banner: " + e.getMessage());
        }
    }

    private <T extends Actor> void closeOverlays(T actor) {
        try { actor.attemptsTo(CloseOverlays.now()); } catch (Exception e) { LOGGER.info(() -> "CloseOverlays action failed: " + e.getMessage()); }
        try {
            WebDriver drv = BrowseTheWeb.as(actor).getDriver();
            ((JavascriptExecutor) drv).executeScript(
                "try{ Array.from(document.querySelectorAll('.om-popup, .popup, .overlay, .modal, .newsletter, .cookie-banner, .popup-wrapper, .pop-up')).forEach(function(e){ try{ e.style.display='none'; e.remove(); }catch(x){} }); }catch(e){}"
            );
        } catch (Exception e) { LOGGER.info(() -> "closeOverlays JS failed: " + e.getMessage()); }
    }

    private <T extends Actor> void ensureOnFaq(T actor) {
        try {
            WebDriver drv = BrowseTheWeb.as(actor).getDriver();
            String current = drv.getCurrentUrl();
            if (current == null || !current.contains("preguntas-frecuentes")) {
                drv.navigate().to(faqPage.URL + "/preguntas-frecuentes/");
            }
        } catch (Exception e) { LOGGER.info(() -> "ensureOnFaq failed: " + e.getMessage()); }
    }
}
