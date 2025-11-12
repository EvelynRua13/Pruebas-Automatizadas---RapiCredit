package com.rapicredit.utils;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actions.Open;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.WebDriver;
import com.rapicredit.interactions.SafeClick;
import com.rapicredit.interactions.CheckLoginSuccess;

import java.util.List;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import net.serenitybdd.core.pages.WebElementFacade;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CommonActions {

    private static final Logger logger = Logger.getLogger(CommonActions.class.getName());
    private static final String JS_CLICK = "arguments[0].click();";

    private CommonActions() {}

    public static void openHome(Actor actor, String url) {
        actor.attemptsTo(Open.url(url));
    }

    public static void clickAndSwitch(Actor actor, Target link) {
        actor.attemptsTo(
                SafeClick.on(link),
                // switch interaction already available in project; use it if present
                com.rapicredit.interactions.SwitchToLastWindow.now()
        );
    }

    public static boolean submitAndCheck(Actor actor, Target submitButton, String successUrlSubstring, int timeoutSeconds) {
        return submitAndCheck(actor, submitButton, successUrlSubstring, null, timeoutSeconds);
    }

    public static boolean submitAndCheck(Actor actor, Target submitButton, String successUrlSubstring, String successIndicatorCss, int timeoutSeconds) {
        String buttonName = submitButton != null ? submitButton.getName() : "<null>";
        logger.log(Level.INFO, "[DEBUG] submitAndCheck: attempting to click submit button: {0}", new Object[]{buttonName});

        WebDriver driver = null;
        try { driver = BrowseTheWeb.as(actor).getDriver(); } catch (Exception ignored) { /* ignore: no driver available */ }

        // registrar URL inicial para detectar cambio después del envío
        String initialUrl = null;
        try { if (driver != null) initialUrl = driver.getCurrentUrl(); } catch (Exception ignored) { /* ignore */ }

        // 1) intentar clicar de forma robusta
        attemptSafeClick(actor, submitButton, driver);
        if (Boolean.getBoolean("login.debug")) {
            try { System.out.println("[login.debug] after click currentUrl=" + (driver==null?"<no-driver>":driver.getCurrentUrl())); }
            catch (Exception ignored) {/* ignore */}

        }
        // 2) esperar brevemente para que comience la navegación
        sleepMs(600);

        // 3) esperar a que la URL cambie
        if (driver != null) {
            waitForUrlChange(driver, initialUrl, successUrlSubstring, timeoutSeconds);
            if (Boolean.getBoolean("login.debug")) {
                try { System.out.println("[login.debug] after waitForUrlChange currentUrl=" + driver.getCurrentUrl()); } catch (Exception ignored) {}
            }
        }

        // 4) si se abrió ventana nueva, cambiar a la última
        if (driver != null) {
            try { switchToLastWindow(driver); } catch (Exception ignored) { /* ignore window switch errors */ }
            if (Boolean.getBoolean("login.debug")) {
                try { System.out.println("[login.debug] after switchToLastWindow currentUrl=" + driver.getCurrentUrl()); } catch (Exception ignored) {}
            }
        }

        // 5) esperar a readyState complete
        if (driver != null) {
            waitForReadyState(driver, timeoutSeconds);
            if (Boolean.getBoolean("login.debug")) {
                try { Object ready = ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return document.readyState"); System.out.println("[login.debug] after waitForReadyState readyState="+ready); } catch (Exception ignored) {}
            }
        }

        // 6) detect success using a dedicated helper to keep this method small
        boolean success = detectSuccess(actor, driver, successUrlSubstring, successIndicatorCss, timeoutSeconds);
        if (Boolean.getBoolean("login.debug")) {
            System.out.println("[login.debug] detectSuccess returned=" + success);
        }

        // sí se detectó éxito, añadir una pequeña espera para dar tiempo al renderizado final antes de devolver
        if (success) {
            try { Thread.sleep(1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }

        if (!success) {
            // No inyectar banner aquí: dejar que los StepDefinitions gestionen la detección del popup/fallas
            logger.log(Level.FINE, "[DEBUG] submitAndCheck: no success detected for button {0}", new Object[]{buttonName});
        }

        return success;
    }

    private static boolean detectSuccess(Actor actor, WebDriver driver, String successUrlSubstring, String successIndicatorCss, int timeoutSeconds) {
        // 1) URL-based detection via interaction (robust)
        try {
            if (successUrlSubstring != null && !successUrlSubstring.isBlank()) {
                actor.attemptsTo(CheckLoginSuccess.whenUrlContains(successUrlSubstring, timeoutSeconds));
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.FINE, "[DEBUG] CheckLoginSuccess wait failed or timed out: {0}", new Object[]{e.getMessage()});
        }

        // 2) indicator element
        if (driver != null && successIndicatorCss != null) {
            if (waitForElementVisible(driver, successIndicatorCss, timeoutSeconds)) return true;
        }
        return false;
    }

    // ---------------- helper methods (extracciones para reducir complejidad) ----------------

    private static void attemptSafeClick(Actor actor, Target submitButton, WebDriver driver) {
        if (submitButton == null) {
            // fallback: intentar encontrar un botón visible en la página
            if (driver != null) {
                findAndClickFallbackButton(driver);
            }
            return;
        }

        try {
            actor.attemptsTo(SafeClick.on(submitButton));
        } catch (Exception e) {
            logger.log(Level.FINE, "[DEBUG] SafeClick on provided target failed: {0}", new Object[]{e.getMessage()});
            // intentar fallback usando elementos resueltos por el Target
            tryClickResolvedTarget(actor, submitButton, driver);
        }
    }

    private static void tryClickResolvedTarget(Actor actor, Target submitButton, WebDriver driver) {
        try {
            List<WebElementFacade> elements = submitButton.resolveAllFor(actor);
            if (!elements.isEmpty() && driver != null) {
                try {
                    ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(JS_CLICK, elements.get(0));
                    logger.fine("[DEBUG] JS click on resolved target performed");
                    return;
                } catch (Exception ex) {
                    logger.log(Level.FINE, "[DEBUG] JS click on resolved target failed: {0}", new Object[]{ex.getMessage()});
                }
            }
        } catch (Exception ex) { logger.log(Level.FINE, "[DEBUG] resolveAllFor failed: {0}", new Object[]{ex.getMessage()}); }

        // fallback general
        if (driver != null) {
            findAndClickFallbackButton(driver);
        }
    }

    private static void findAndClickFallbackButton(WebDriver driver) {
        List<WebElement> buttons;
        try {
            buttons = driver.findElements(By.cssSelector("button[type='submit'], input[type='submit'], button, .btn"));
        } catch (Exception ex) {
            logger.log(Level.FINE, "[DEBUG] searching for fallback buttons failed: {0}", new Object[]{ex.getMessage()});
            return;
        }

        for (WebElement b : buttons) {
            try {
                if (!b.isDisplayed()) continue;
                try {
                    ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(JS_CLICK, b);
                    logger.fine("[DEBUG] JS click on fallback button performed");
                    return;
                } catch (Exception ex) {
                    logger.log(Level.FINE, "[DEBUG] JS click on fallback button failed: {0}", new Object[]{ex.getMessage()});
                    // intentar siguiente
                }
            } catch (Exception ex) { logger.log(Level.FINE, "[DEBUG] element read failed: {0}", new Object[]{ex.getMessage()}); }
        }
    }

    @SuppressWarnings("squid:S2925")
    private static void sleepMs(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    private static void waitForUrlChange(WebDriver driver, String initialUrl, String successUrlSubstring, int timeoutSeconds) {
        // ampliar comprobaciones: revisar durante timeoutSeconds*3 para tolerar redirecciones lentas
        for (int i = 0; i < timeoutSeconds * 3; i++) { // checks every 500ms
            try {
                String current = driver.getCurrentUrl();
                if (current != null) {
                    // usar equals sobre initialUrl (que puede ser null) de forma segura
                    if (initialUrl == null || !initialUrl.equals(current)) {
                        return;
                    }
                    if (successUrlSubstring != null && !successUrlSubstring.isBlank() && current.contains(successUrlSubstring)) {
                        return;
                    }
                }
            } catch (Exception ignored) { /* ignore url read errors */ }
            sleepMs(500);
        }
    }

    private static void switchToLastWindow(WebDriver driver) {
        String last = null;
        for (String h : driver.getWindowHandles()) { last = h; }
        if (last != null) {
            try { driver.switchTo().window(last); } catch (Exception ignored) { /* ignore */ }
        }
    }

    private static void waitForReadyState(WebDriver driver, int timeoutSeconds) {
        for (int i = 0; i < timeoutSeconds * 2; i++) {
            try {
                Object ready = ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return document.readyState");
                if (ready != null && "complete".equalsIgnoreCase(ready.toString())) {
                    return;
                }
            } catch (Exception ignored) { /* ignore readyState read errors */ }
            sleepMs(500);
        }
    }

    private static boolean anyDisplayed(List<WebElement> nodes) {
        if (nodes == null || nodes.isEmpty()) return false;
        for (WebElement n : nodes) {
            try { if (n.isDisplayed()) return true; } catch (Exception ignored) { /* ignore element read errors */ }
        }
        return false;
    }

    private static boolean waitForElementVisible(WebDriver driver, String cssSelector, int timeoutSeconds) {
        for (int i = 0; i < timeoutSeconds * 2; i++) {
            try {
                List<WebElement> nodes = driver.findElements(By.cssSelector(cssSelector));
                if (anyDisplayed(nodes)) return true;
            } catch (Exception ignored) { /* ignore selector errors */ }
            sleepMs(500);
        }
        return false;
    }

}
